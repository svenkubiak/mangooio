package io.mangoo.routing.handlers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import freemarker.template.TemplateException;
import io.mangoo.annotations.FilterWith;
import io.mangoo.authentication.Authentication;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.Binding;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.utils.CookieBuilder;
import io.mangoo.utils.JsonUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.FormParserFactory.Builder;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

/**
 * Main class that handles all controller requests
 *
 * @author skubiak
 *
 */
public class RequestHandler implements HttpHandler {
    private static final int AUTH_PREFIX_LENGTH = 3;
    private static final int TOKEN_LENGTH = 16;
    private static final int INDEX_0 = 0;
    private static final int INDEX_1 = 1;
    private static final int INDEX_2 = 2;
    private static final int INDEX_3 = 3;
    private static final int SESSION_PREFIX_LENGTH = 4;
    private final int parameterCount;
    private final Class<?> controllerClass;
    private final String controllerMethod;
    private final Object controller;
    private final Map<String, Class<?>> methodParameters;
    private Method method;
    private Authentication authentication;
    private Session session;
    private Flash flash;
    private Form form;
    private Request request;
    private Map<String, String> requestParameter;
    private String body = "";
    private final boolean hasRequestFilter;
    private final boolean async;
    private final Config config;

    public RequestHandler(Class<?> controllerClass, String controllerMethod, boolean async) {
        this.controllerClass = Objects.requireNonNull(controllerClass, "controllerClass can not be null");
        this.controllerMethod = Objects.requireNonNull(controllerMethod, "controllerMethod can not be null");
        this.async = async;
        this.controller = Application.getInstance(this.controllerClass);
        this.methodParameters = getMethodParameters();
        this.parameterCount = this.methodParameters.size();
        this.hasRequestFilter = Application.getInjector().getAllBindings().containsKey(com.google.inject.Key.get(MangooRequestFilter.class));
        this.config = Application.getInstance(Config.class);
    }

    @Override
    @SuppressWarnings("all")
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if ( (RequestUtils.isPostOrPut(exchange) || this.async) && exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        this.method = this.controller.getClass().getMethod(this.controllerMethod, methodParameters.values().toArray(new Class[0]));
        this.requestParameter = RequestUtils.getRequestParameters(exchange);

        setLocale(exchange);
        getSessionCookie(exchange);
        getAuthenticationCookie(exchange);
        getFlashCookie(exchange);
        getForm(exchange);
        getRequestBody(exchange);
        getRequest(exchange);

        final Response response = getResponse(exchange);
        response.getCookies().forEach(cookie -> exchange.setResponseCookie(cookie));

        setSessionCookie(exchange);
        setFlashCookie(exchange);
        setAuthenticationCookie(exchange);

        if (response.isRedirect()) {
            handleRedirectResponse(exchange, response);
        } else if (response.isBinary()) {
            handleBinaryResponse(exchange, response);
        } else {
            handleRenderedResponse(exchange, response);
        }
    }

    /**
     * Retrieves the locale from the current request or sets a default one and
     * triggers a reload of the Messages class if the locale has changed.
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void setLocale(HttpServerExchange exchange) {
        final Messages messages = Application.getInstance(Messages.class);
        final HeaderValues headerValues = exchange.getRequestHeaders().get(Headers.ACCEPT_LANGUAGE_STRING);
        if (headerValues == null) {
            Locale.setDefault(Locale.forLanguageTag(this.config.getApplicationLanguage()));
        } else if (headerValues.getFirst() != null) {
            final String values = Optional.ofNullable(headerValues.getFirst()).orElse("");
            final Iterable<String> split = Splitter.on(",").trimResults().split(values);
            if (split == null) {
                Locale.setDefault(Locale.forLanguageTag(this.config.getApplicationLanguage()));
            } else {
                final String acceptLanguage = Optional.ofNullable(split.iterator().next()).orElse(this.config.getApplicationLanguage());
                Locale.setDefault(Locale.forLanguageTag(acceptLanguage.substring(0, 2))); //NOSONAR
            }
        }

        messages.reload();
    }

    /**
     * Execute filters if exists in the following order:
     * RequestFilter, ControllerFilter, MethodFilter
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A Response object that will be merged to the final response
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws TemplateException
     * @throws IOException
     */
    private Response getResponse(HttpServerExchange exchange) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException, TemplateException {
        //execute global request filter
        Response response = Response.withOk();
        if (this.hasRequestFilter) {
            final MangooRequestFilter mangooRequestFilter = Application.getInstance(MangooRequestFilter.class);
            response = mangooRequestFilter.execute(this.request, response);
        }

        if (response.isEndResponse()) {
            return response;
        }

        //execute controller filters
        response = executeFilter(this.controllerClass.getAnnotations(), response);
        if (response.isEndResponse()) {
            return response;
        }

        //execute method filters
        response = executeFilter(this.method.getAnnotations(), response);
        if (response.isEndResponse()) {
            return response;
        }

        return invokeController(exchange, response);
    }

    /**
     * Creates a new request object containing the current request data
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void getRequest(HttpServerExchange exchange) {
        final String authenticityToken = Optional.ofNullable(this.requestParameter.get(Default.AUTHENTICITY_TOKEN.toString())).orElse(this.form.get(Default.AUTHENTICITY_TOKEN.toString()));
        this.request = new Request(exchange, this.session, authenticityToken, this.authentication, this.requestParameter, this.body);
    }

    /**
     * Executes all filters on controller and method level
     *
     * @param annotations An array of @FilterWith annotated classes and methods
     * @param response
     * @return True if the request should continue after filter execution, false otherwise
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Response executeFilter(Annotation[] annotations, Response response) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (final Annotation annotation : annotations) {
            if (annotation.annotationType().equals(FilterWith.class)) {
                final FilterWith filterWith = (FilterWith) annotation;
                for (final Class<?> clazz : filterWith.value()) {
                    if (response.isEndResponse()) {
                        return response;
                    } else {
                        final Method classMethod = clazz.getMethod(Default.FILTER_METHOD.toString(), Request.class, Response.class);
                        response = (Response) classMethod.invoke(Application.getInstance(clazz), this.request, response);
                    }
                }
            }
        }

        return response;
    }

    /**
     * Invokes the controller methods and retrieves the response which
     * is later send to the client
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A response object
     *
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     * @throws TemplateException
     */
    private Response invokeController(HttpServerExchange exchange, Response response) throws IllegalAccessException, InvocationTargetException, IOException, TemplateException {
        Response invokedResponse;

        if (this.methodParameters.isEmpty()) {
            invokedResponse = (Response) this.method.invoke(this.controller);
        } else {
            final Object [] convertedParameters = getConvertedParameters(exchange);
            invokedResponse = (Response) this.method.invoke(this.controller, convertedParameters);
        }

        invokedResponse.andContent(response.getContent());
        invokedResponse.andHeaders(response.getHeaders());
        if (!invokedResponse.isRendered()) {
            final TemplateEngine templateEngine = Application.getInstance(TemplateEngine.class);
            invokedResponse.andBody(templateEngine.render(this.flash, this.session, this.form, Application.getInstance(Messages.class), getTemplatePath(invokedResponse), invokedResponse.getContent()));
        }

        return invokedResponse;
    }

    /**
     * Returns the complete path to the template based on the
     * controller and method name
     *
     * @param response The current response
     *
     * @return A case-sensitive template path, e.g. /ApplicationController/index.ftl
     */
    private String getTemplatePath(Response response) {
        return StringUtils.isBlank(response.getTemplate()) ? (this.controllerClass.getSimpleName() + "/" + RequestUtils.getTemplateName(this.method.getName())) : response.getTemplate();
    }

    /**
     * Retrieves the current session from the HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void getSessionCookie(HttpServerExchange exchange) {
        Session requestSession = null;
        final Cookie cookie = exchange.getRequestCookies().get(this.config.getSessionCookieName());
        if (cookie != null) {
            String cookieValue = cookie.getValue();
            if (StringUtils.isNotBlank(cookieValue) && !("null").equals(cookieValue)) {
                if (this.config.isSessionCookieEncrypt()) {
                    final Crypto crypto = Application.getInstance(Crypto.class);
                    cookieValue = crypto.decrypt(cookieValue);
                }

                String sign = null;
                String expires = null;
                String authenticityToken = null;
                String version = null;
                final String prefix = StringUtils.substringBefore(cookieValue, Default.DATA_DELIMITER.toString());
                if (StringUtils.isNotBlank(prefix)) {
                    final String [] prefixes = prefix.split("\\" + Default.DELIMITER.toString());

                    if (prefixes != null && prefixes.length == SESSION_PREFIX_LENGTH) {
                        sign = prefixes [INDEX_0];
                        authenticityToken = prefixes [INDEX_1];
                        expires = prefixes [INDEX_2];
                        version = prefixes [INDEX_3];
                    }
                }

                if (StringUtils.isNotBlank(sign) && StringUtils.isNotBlank(expires) && StringUtils.isNotBlank(authenticityToken)) {
                    final String data = cookieValue.substring(cookieValue.indexOf(Default.DATA_DELIMITER.toString()) + 1, cookieValue.length());
                    final LocalDateTime expiresDate = LocalDateTime.parse(expires);

                    if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(data + authenticityToken + expires + version + this.config.getApplicationSecret()).equals(sign)) {
                        final Map<String, String> sessionValues = new HashMap<>();
                        if (StringUtils.isNotEmpty(data)) {
                            for (final Map.Entry<String, String> entry : Splitter.on(Default.SPLITTER.toString()).withKeyValueSeparator(Default.SEPERATOR.toString()).split(data).entrySet()) {
                                sessionValues.put(entry.getKey(), entry.getValue());
                            }
                        }
                        requestSession = new Session(sessionValues, authenticityToken, expiresDate);
                    }
                }
            }
        }

        if (requestSession == null) {
            requestSession = new Session(new HashMap<>(), RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH), LocalDateTime.now().plusSeconds(this.config.getSessionExpires()));
        }

        this.session = requestSession;
    }

    /**
     * Sets the session cookie to the current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void setSessionCookie(HttpServerExchange exchange) {
        if (this.session != null && this.session.hasChanges()) {
            final String data = Joiner.on(Default.SPLITTER.toString()).withKeyValueSeparator(Default.SEPERATOR.toString()).join(this.session.getValues());
            final String version = this.config.getCookieVersion();
            final String authenticityToken = this.session.getAuthenticityToken();
            final LocalDateTime expires = this.session.getExpires();
            final StringBuilder buffer = new StringBuilder()
                    .append(DigestUtils.sha512Hex(data + authenticityToken + expires + version + this.config.getApplicationSecret()))
                    .append(Default.DELIMITER.toString())
                    .append(authenticityToken)
                    .append(Default.DELIMITER.toString())
                    .append(expires)
                    .append(Default.DELIMITER.toString())
                    .append(version)
                    .append(Default.DATA_DELIMITER.toString())
                    .append(data);

            String value = buffer.toString();
            if (this.config.isAuthenticationCookieEncrypt()) {
                value = Application.getInstance(Crypto.class).encrypt(value);
            }

            final Cookie cookie = CookieBuilder.create()
                .name(this.config.getSessionCookieName())
                .value(value)
                .secure(this.config.isSessionCookieSecure())
                .httpOnly(true)
                .expires(expires)
                .build();

            exchange.setResponseCookie(cookie);
        }
    }

    /**
     * Retrieves the current authentication from the HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void getAuthenticationCookie(HttpServerExchange exchange) {
        Authentication requestAuthentication = null;
        final Cookie cookie = exchange.getRequestCookies().get(this.config.getAuthenticationCookieName());
        if (cookie != null) {
            String cookieValue = cookie.getValue();
            if (StringUtils.isNotBlank(cookieValue) && !("null").equals(cookieValue)) {
                if (this.config.isAuthenticationCookieEncrypt()) {
                    cookieValue = Application.getInstance(Crypto.class).decrypt(cookieValue);
                }

                String sign = null;
                String expires = null;
                String version = null;
                final String prefix = StringUtils.substringBefore(cookieValue, Default.DATA_DELIMITER.toString());
                if (StringUtils.isNotBlank(prefix)) {
                    final String [] prefixes = prefix.split("\\" + Default.DELIMITER.toString());

                    if (prefixes != null && prefixes.length == AUTH_PREFIX_LENGTH) {
                        sign = prefixes [INDEX_0];
                        expires = prefixes [INDEX_1];
                        version = prefixes [INDEX_2];
                    }
                }

                if (StringUtils.isNotBlank(sign) && StringUtils.isNotBlank(expires)) {
                    final String authenticatedUser = cookieValue.substring(cookieValue.indexOf(Default.DATA_DELIMITER.toString()) + 1, cookieValue.length());
                    final LocalDateTime expiresDate = LocalDateTime.parse(expires);

                    if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(authenticatedUser + expires + version + this.config.getApplicationSecret()).equals(sign)) {
                        requestAuthentication = new Authentication(expiresDate, authenticatedUser);
                    }
                }
            }
        }

        if (requestAuthentication == null) {
            requestAuthentication = new Authentication(LocalDateTime.now().plusSeconds(this.config.getAuthenticationExpires()), null);
        }

        this.authentication = requestAuthentication;
    }

    /**
     * Sets the authentication cookie to the current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void setAuthenticationCookie(HttpServerExchange exchange) {
        if (this.authentication != null && this.authentication.hasAuthenticatedUser()) {
            Cookie cookie;
            final String cookieName = this.config.getAuthenticationCookieName();
            if (this.authentication.isLogout()) {
                cookie = exchange.getRequestCookies().get(cookieName);
                cookie.setSecure(this.config.isAuthenticationCookieSecure());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                cookie.setDiscard(true);
            } else {
                final String authenticatedUser = this.authentication.getAuthenticatedUser();
                final LocalDateTime expires = this.authentication.isRemember() ? LocalDateTime.now().plusSeconds(this.config.getAuthenticationRememberExpires()) : this.authentication.getExpires();
                final String version = this.config.getAuthCookieVersion();
                final String sign = DigestUtils.sha512Hex(authenticatedUser + expires + version + this.config.getApplicationSecret());

                final StringBuilder buffer = new StringBuilder()
                        .append(sign)
                        .append(Default.DELIMITER.toString())
                        .append(expires)
                        .append(Default.DELIMITER.toString())
                        .append(version)
                        .append(Default.DATA_DELIMITER.toString())
                        .append(authenticatedUser);

                String value = buffer.toString();
                if (this.config.isAuthenticationCookieEncrypt()) {
                    value = Application.getInstance(Crypto.class).encrypt(value);
                }

                cookie = CookieBuilder.create()
                        .name(cookieName)
                        .value(value)
                        .secure(this.config.isAuthenticationCookieSecure())
                        .httpOnly(true)
                        .expires(expires)
                        .build();
            }

            exchange.setResponseCookie(cookie);
        }
    }

    /**
     * Retrieves the flash cookie from the current
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void getFlashCookie(HttpServerExchange exchange) {
        Flash requestFlash = null;
        final Cookie cookie = exchange.getRequestCookies().get(this.config.getFlashCookieName());
        if (cookie != null){
            final String cookieValue = cookie.getValue();
            if (StringUtils.isNotEmpty(cookieValue) && !("null").equals(cookieValue)) {
                final Map<String, String> values = new HashMap<>();
                for (final Map.Entry<String, String> entry : Splitter.on("&").withKeyValueSeparator(":").split(cookie.getValue()).entrySet()) {
                    values.put(entry.getKey(), entry.getValue());
                }

                requestFlash = new Flash(values);
                requestFlash.setDiscard(true);
            }
        }

        if (requestFlash == null) {
            requestFlash = new Flash();
        }

        this.flash = requestFlash;
    }

    /**
     * Sets the flash cookie to current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void setFlashCookie(HttpServerExchange exchange) {
        if (this.flash != null && !this.flash.isDiscard() && this.flash.hasContent()) {
            final String values = Joiner.on("&").withKeyValueSeparator(":").join(this.flash.getValues());

            final Cookie cookie = CookieBuilder.create()
                    .name(this.config.getFlashCookieName())
                    .value(values)
                    .secure(this.config.isFlashCookieSecure())
                    .httpOnly(true)
                    .build();

            exchange.setResponseCookie(cookie);
        } else {
            final Cookie cookie = exchange.getRequestCookies().get(this.config.getFlashCookieName());
            if (cookie != null) {
                cookie.setHttpOnly(true)
                .setSecure(this.config.isFlashCookieSecure())
                .setPath("/")
                .setMaxAge(0);

                exchange.setResponseCookie(cookie);
            }
        }
    }

    /**
     * Retrieves the form parameter from a request
     *
     * @param exchange The Undertow HttpServerExchange
     *
     * @throws IOException
     */
    private void getForm(HttpServerExchange exchange) throws IOException {
        this.form = Application.getInstance(Form.class);
        if (RequestUtils.isPostOrPut(exchange)) {
            final Builder builder = FormParserFactory.builder();
            builder.setDefaultCharset(Charsets.UTF_8.name());

            final FormDataParser formDataParser = builder.build().createParser(exchange);
            if (formDataParser != null) {
                exchange.startBlocking();
                final FormData formData = formDataParser.parseBlocking();

                for (final String data : formData) {
                    for (final FormData.FormValue formValue : formData.get(data)) {
                        if (formValue.isFile()) {
                            this.form.addFile(formValue.getPath().toFile());
                        } else {
                            this.form.addValue(new HttpString(data).toString(), formValue.getValue());
                        }
                    }
                }

                this.form.setSubmitted(true);
            }
        }
    }

    /**
     * Retrieves the complete request body from the request
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A body object containing the request body
     *
     * @throws IOException
     */
    private void getRequestBody(HttpServerExchange exchange) throws IOException {
        if (RequestUtils.isPostOrPut(exchange)) {
            exchange.startBlocking();
            this.body = IOUtils.toString(exchange.getInputStream());
        }
    }

    /**
     * Creates an array with the request controller method parameter and sets the appropriate values
     *
     * @param exchange The Undertow HttpServerExchange
     * @return an array with the request controller method parameter and sets the appropriate values
     *
     * @throws IOException
     */
    private Object[] getConvertedParameters(HttpServerExchange exchange) throws IOException {
        final Object [] convertedParameters = new Object[this.parameterCount];

        int index = 0;
        for (final Map.Entry<String, Class<?>> entry : this.methodParameters.entrySet()) {
            final String key = entry.getKey();
            final Class<?> clazz = entry.getValue();
            final Binding binding = Optional.ofNullable(Binding.fromString(clazz.getName())).orElse(Binding.UNDEFINED);

            switch (binding) {
            case FORM:
                convertedParameters[index] = this.form;
                break;
            case AUTHENTICATION:
                convertedParameters[index] = this.authentication;
                break;
            case SESSION:
                convertedParameters[index] = this.session;
                break;
            case FLASH:
                convertedParameters[index] = this.flash;
                break;
            case REQUEST:
                convertedParameters[index] = this.request;
                break;
            case LOCALDATE:
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? null : LocalDate.parse(this.requestParameter.get(key));
                break;
            case LOCALDATETIME:
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? null : LocalDateTime.parse(this.requestParameter.get(key));
                break;
            case STRING:
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? null : this.requestParameter.get(key);
                break;
            case INT_PRIMITIVE:
            case INTEGER:
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? null : Integer.valueOf(this.requestParameter.get(key));
                break;
            case DOUBLE_PRIMITIVE:
            case DOUBLE:
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? null : Double.valueOf(this.requestParameter.get(key));
                break;
            case FLOAT_PRIMITIVE:
            case FLOAT:
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? null : Float.valueOf(this.requestParameter.get(key));
                break;
            case LONG_PRIMITIVE:
            case LONG:
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? null : Long.valueOf(this.requestParameter.get(key));
                break;
            case UNDEFINED:
                convertedParameters[index] = RequestUtils.isJsonRequest(exchange) ? JsonUtils.fromJson(this.body, clazz) : null;
                break;
            default:
                convertedParameters[index] = null;
                break;
            }

            index++;
        }

        return convertedParameters;
    }

    /**
     * Converts the method parameter of a mapped controller method to a map
     *
     * @return A Map containing the declared methods of the method parameters and ther class type
     */
    private Map<String, Class<?>> getMethodParameters() {
        final Map<String, Class<?>> parameters = new LinkedHashMap<>(); //NOSONAR
        for (final Method declaredMethod : this.controller.getClass().getDeclaredMethods()) {
            if (declaredMethod.getName().equals(this.controllerMethod) && declaredMethod.getParameterCount() > 0) {
                Arrays.asList(declaredMethod.getParameters()).forEach(parameter -> parameters.put(parameter.getName(), parameter.getType())); //NOSONAR
                break;
            }
        }

        return parameters;
    }

    /**
     * Handles a redirect response to the client by sending a 403 status code to the client
     *
     * @param exchange The Undertow HttpServerExchange
     * @param response The response object
     */
    private void handleRedirectResponse(HttpServerExchange exchange, Response response) {
        exchange.setStatusCode(StatusCodes.FOUND);
        exchange.getResponseHeaders().put(Headers.LOCATION, response.getRedirectTo());
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value)); //NOSONAR
        exchange.endExchange();
    }

    /**
     * Handles a rendered response to the client by sending the rendered body from the response object
     *
     * @param exchange The Undertow HttpServerExchange
     * @param response The response object
     */
    private void handleRenderedResponse(HttpServerExchange exchange, Response response) {
        exchange.setStatusCode(response.getStatusCode());
        exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), Default.X_XSS_PPROTECTION.toInt());
        exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), Default.NOSNIFF.toString());
        exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), Default.SAMEORIGIN.toString());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, response.getContentType() + "; charset=" + response.getCharset());
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value)); //NOSONAR
        exchange.getResponseSender().send(getResponseBody(exchange, response));
    }

    /**
     * Handles a binary response to the client by sending the binary content from the response
     * to the undertow output stream
     *
     * @param exchange The Undertow HttpServerExchange
     * @param response The response object
     *
     * @throws IOException
     */
    private void handleBinaryResponse(HttpServerExchange exchange, Response response) throws IOException {
        exchange.dispatch(exchange.getDispatchExecutor(), new BinaryHandler(response));
    }

    /**
     * Retrieves the body of the request and checks i an ETag needs to be handled
     *
     * @param exchange The HttpServerExchange
     * @param response The Response object
     * @return The body from the response object or an empty body if etag matches NONE_MATCH header
     */
    private String getResponseBody(HttpServerExchange exchange, Response response) {
        String responseBody = response.getBody();
        if (response.isETag()) {
            final String noneMatch = exchange.getRequestHeaders().getFirst(Headers.IF_NONE_MATCH_STRING);
            final String etag = DigestUtils.md5Hex(responseBody); //NOSONAR
            if (StringUtils.isNotBlank(noneMatch) && StringUtils.isNotBlank(etag) && noneMatch.equals(etag)) {
                exchange.setStatusCode(StatusCodes.NOT_MODIFIED);
                responseBody = "";
            } else {
                exchange.getResponseHeaders().put(Headers.ETAG, etag);
            }
        }

        return responseBody;
    }
}