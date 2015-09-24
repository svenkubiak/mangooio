package io.mangoo.routing.handlers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.inject.Injector;

import freemarker.template.TemplateException;
import io.mangoo.annotations.FilterWith;
import io.mangoo.authentication.Authentication;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.Binding;
import io.mangoo.enums.ContentType;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.enums.Key;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.FormParserFactory.Builder;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

/**
 *
 * @author skubiak
 *
 */
public class RequestHandler implements HttpHandler {
    private static final int AUTH_PREFIX_LENGTH = 2;
    private static final int TOKEN_LENGTH = 16;
    private static final int INDEX_0 = 0;
    private static final int INDEX_1 = 1;
    private static final int INDEX_2 = 2;
    private static final int INDEX_3 = 3;
    private static final int SESSION_PREFIX_LENGTH = 3;
    private int parameterCount;
    private Class<?> controllerClass;
    private String controllerMethod;
    private Object controller;
    private Map<String, Class<?>> methodParameters;
    private Method method;
    private ObjectMapper opjectMapper;
    private Authentication authentication;
    private Session session;
    private Flash flash;
    private Form form;
    private Config config;
    private Injector injector;
    private Request request;
    private boolean hasRequestFilter;
    private Map<String, String> requestParameter;
    private String body = "";

    public RequestHandler(Class<?> controllerClass, String controllerMethod) {
        this.injector = Application.getInjector();
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
        this.controller = this.injector.getInstance(this.controllerClass);
        this.methodParameters = getMethodParameters();
        this.parameterCount = this.methodParameters.size();
        this.config = this.injector.getInstance(Config.class);
        this.hasRequestFilter = this.injector.getAllBindings().containsKey(com.google.inject.Key.get(MangooRequestFilter.class));
        this.opjectMapper = JsonFactory.create();
    }

    @Override
    @SuppressWarnings("all")
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.method = this.controller.getClass().getMethod(this.controllerMethod, methodParameters.values().toArray(new Class[0]));
        this.requestParameter = RequestUtils.getRequestParameters(exchange);

        setLocale(exchange);
        getSession(exchange);
        getAuthentication(exchange);
        getFlash(exchange);
        getForm(exchange);
        getRequestBody(exchange);
        getRequest(exchange);

        Response response = getResponse(exchange);

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
        HeaderValues headerValues = exchange.getRequestHeaders().get(Headers.ACCEPT_LANGUAGE_STRING);
        if (headerValues != null && headerValues.getFirst() != null) {
            Iterable<String> split = Splitter.on(",").trimResults().split(headerValues.getFirst());
            if (split != null) {
                String language = Optional.ofNullable(split.iterator().next()).orElse(this.config.getApplicationLanguage());
                Locale.setDefault(Locale.forLanguageTag(language.substring(0, 1)));
                this.injector.getInstance(Messages.class).reload();
            }
        }
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
            MangooRequestFilter mangooRequestFilter = this.injector.getInstance(MangooRequestFilter.class);
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
        String authenticityToken = Optional.ofNullable(this.requestParameter.get(Default.AUTHENTICITY_TOKEN.toString())).orElse(this.form.getValue(Default.AUTHENTICITY_TOKEN.toString()));
        this.request = new Request(exchange, this.session, authenticityToken, this.authentication, this.requestParameter, this.body);
    }

    /**
     * Executes all filters on controller and method level
     *
     * @param annotations An array of @FilterWith annotated classes and methods
     * @param response
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request should continue after filter execution, false otherwise
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Response executeFilter(Annotation[] annotations, Response response) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        FilterWith filterWith = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(FilterWith.class)) {
                filterWith = (FilterWith) annotation;
                for (Class<?> clazz : filterWith.value()) {
                    if (response.isEndResponse()) {
                        return response;
                    } else {
                        Method classMethod = clazz.getMethod(Default.FILTER_METHOD.toString(), Request.class, Response.class);
                        response = (Response) classMethod.invoke(this.injector.getInstance(clazz), this.request, response);
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
     * @param response2
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
            Object [] convertedParameters = getConvertedParameters(exchange);
            invokedResponse = (Response) this.method.invoke(this.controller, convertedParameters);
        }

        invokedResponse.andContent(response.getContent());
        invokedResponse.andHeaders(response.getHeaders());
        if (!invokedResponse.isRendered()) {
            TemplateEngine templateEngine = this.injector.getInstance(TemplateEngine.class);
            invokedResponse.andBody(templateEngine.render(this.flash, this.session, this.form, this.injector.getInstance(Messages.class), getTemplatePath(invokedResponse), invokedResponse.getContent()));
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
        return StringUtils.isBlank(response.getTemplate()) ? this.controllerClass.getSimpleName() + "/" + RequestUtils.getTemplateName(this.method.getName()) : response.getTemplate();
    }

    /**
     * Retrieves the current session from the HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void getSession(HttpServerExchange exchange) {
        Session requestSession = null;
        Cookie cookie = exchange.getRequestCookies().get(this.config.getSessionCookieName());
        if (cookie != null) {
            String cookieValue = cookie.getValue();
            if (StringUtils.isNotBlank(cookieValue)) {
                if (this.config.getBoolean(Key.COOKIE_ENCRYPTION, false)) {
                    Crypto crypto = this.injector.getInstance(Crypto.class);
                    cookieValue = crypto.decrypt(cookieValue);
                }

                String sign = null;
                String expires = null;
                String authenticityToken = null;
                String version = null;
                String prefix = StringUtils.substringBefore(cookieValue, Default.DATA_DELIMITER.toString());
                if (StringUtils.isNotBlank(prefix)) {
                    String [] prefixes = prefix.split("\\" + Default.DELIMITER.toString());

                    /**
                     * TODO This should be refactored as the else if was only introduced due to compatibility reasons
                     * introduced with version 1.2.0 for cookie versioning.
                     */
                    if (prefixes != null && prefixes.length == SESSION_PREFIX_LENGTH) {
                        sign = prefixes [INDEX_0];
                        authenticityToken = prefixes [INDEX_1];
                        expires = prefixes [INDEX_2];
                    } else if (prefixes != null && prefixes.length == SESSION_PREFIX_LENGTH + 1) {
                        sign = prefixes [INDEX_0];
                        authenticityToken = prefixes [INDEX_1];
                        expires = prefixes [INDEX_2];
                        version = prefixes [INDEX_3];
                    }
                }

                if (StringUtils.isNotBlank(sign) && StringUtils.isNotBlank(expires) && StringUtils.isNotBlank(authenticityToken)) {
                    String data = cookieValue.substring(cookieValue.indexOf(Default.DATA_DELIMITER.toString()) + 1, cookieValue.length());
                    LocalDateTime expiresDate = LocalDateTime.parse(expires);

                    /**
                     * TODO Like above. Old version without cookie versioning and new version with cookie versioning
                     */
                    if (StringUtils.isBlank(version)) {
                        if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(data + authenticityToken + expires + this.config.getApplicationSecret()).equals(sign)) {
                            Map<String, String> sessionValues = new HashMap<String, String>();
                            if (StringUtils.isNotEmpty(data)) {
                                for (Map.Entry<String, String> entry : Splitter.on(Default.SPLITTER.toString()).withKeyValueSeparator(Default.SEPERATOR.toString()).split(data).entrySet()) {
                                    sessionValues.put(entry.getKey(), entry.getValue());
                                }
                            }
                            requestSession = new Session(sessionValues, authenticityToken, expiresDate);
                        }
                    } else {
                        if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(data + authenticityToken + expires + version + this.config.getApplicationSecret()).equals(sign)) {
                            Map<String, String> sessionValues = new HashMap<String, String>();
                            if (StringUtils.isNotEmpty(data)) {
                                for (Map.Entry<String, String> entry : Splitter.on(Default.SPLITTER.toString()).withKeyValueSeparator(Default.SEPERATOR.toString()).split(data).entrySet()) {
                                    sessionValues.put(entry.getKey(), entry.getValue());
                                }
                            }
                            requestSession = new Session(sessionValues, authenticityToken, expiresDate);
                        }
                    }
                }
            }
        }

        if (requestSession == null) {
            requestSession = new Session(new HashMap<String, String>(), RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH), LocalDateTime.now().plusSeconds(this.config.getSessionExpires()));
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
            String data = Joiner.on(Default.SPLITTER.toString()).withKeyValueSeparator(Default.SEPERATOR.toString()).join(this.session.getValues());

            String version = this.config.getCookieVersion();
            String authenticityToken = this.session.getAuthenticityToken();
            LocalDateTime expires = this.session.getExpires();
            StringBuilder buffer = new StringBuilder()
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
                value = this.injector.getInstance(Crypto.class).encrypt(value);
            }

            Cookie cookie = new CookieImpl(this.config.getString(Key.COOKIE_NAME), value)
                    .setSecure(this.config.isSessionCookieSecure())
                    .setHttpOnly(true)
                    .setPath("/")
                    .setExpires(Date.from(expires.atZone(ZoneId.systemDefault()).toInstant()));

            exchange.setResponseCookie(cookie);
        }
    }

    /**
     * Retrieves the current authentication from the HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void getAuthentication(HttpServerExchange exchange) {
        Authentication requestAuthentication = null;
        Cookie cookie = exchange.getRequestCookies().get(this.config.getAuthenticationCookieName());
        if (cookie != null) {
            String cookieValue = cookie.getValue();
            if (StringUtils.isNotBlank(cookieValue)) {
                if (this.config.isAuthenticationCookieEncrypt()) {
                    cookieValue = this.injector.getInstance(Crypto.class).decrypt(cookieValue);
                }

                String sign = null;
                String expires = null;
                String version = null;
                String prefix = StringUtils.substringBefore(cookieValue, Default.DATA_DELIMITER.toString());
                if (StringUtils.isNotBlank(prefix)) {
                    String [] prefixes = prefix.split("\\" + Default.DELIMITER.toString());

                    /**
                     * TODO This should be refactored as the else if was only introduced due to compatibility reasons
                     * introduced with version 1.2.0 for cookie versioning.
                     */
                    if (prefixes != null && prefixes.length == AUTH_PREFIX_LENGTH) {
                        sign = prefixes [INDEX_0];
                        expires = prefixes [INDEX_1];
                    } else if (prefixes != null && prefixes.length == (AUTH_PREFIX_LENGTH + 1)) {
                        sign = prefixes [INDEX_0];
                        expires = prefixes [INDEX_1];
                        version = prefixes [INDEX_2];
                    }
                }

                if (StringUtils.isNotBlank(sign) && StringUtils.isNotBlank(expires)) {
                    String authenticatedUser = cookieValue.substring(cookieValue.indexOf(Default.DATA_DELIMITER.toString()) + 1, cookieValue.length());
                    LocalDateTime expiresDate = LocalDateTime.parse(expires);

                    /**
                     * TODO Like above. Old version without cookie versioning and new version with cookie versioning
                     */
                    if (StringUtils.isBlank(version)) {
                        if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(authenticatedUser + expires + this.config.getApplicationSecret()).equals(sign)) {
                            requestAuthentication = new Authentication(expiresDate, authenticatedUser);
                        }
                    } else {
                        if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(authenticatedUser + expires + version + this.config.getApplicationSecret()).equals(sign)) {
                            requestAuthentication = new Authentication(expiresDate, authenticatedUser);
                        }
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
            String cookieName = this.config.getAuthenticationCookieName();
            if (this.authentication.isLogout()) {
                cookie = exchange.getRequestCookies().get(cookieName);
                cookie.setSecure(this.config.isAuthenticationCookieSecure());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                cookie.setDiscard(true);
            } else {
                String authenticatedUser = this.authentication.getAuthenticatedUser();
                LocalDateTime expires = this.authentication.getExpires();
                String version = this.config.getAuthCookieVersion();
                String sign = DigestUtils.sha512Hex(authenticatedUser + expires + version + this.config.getApplicationSecret());

                StringBuilder buffer = new StringBuilder()
                        .append(sign)
                        .append(Default.DELIMITER.toString())
                        .append(expires)
                        .append(Default.DELIMITER.toString())
                        .append(version)
                        .append(Default.DATA_DELIMITER.toString())
                        .append(authenticatedUser);

                String value = buffer.toString();
                if (this.config.isAuthenticationCookieEncrypt()) {
                    value = this.injector.getInstance(Crypto.class).encrypt(value);
                }

                cookie = new CookieImpl(cookieName, value)
                        .setSecure(this.config.isAuthenticationCookieSecure())
                        .setHttpOnly(true)
                        .setPath("/")
                        .setExpires(Date.from(expires.atZone(ZoneId.systemDefault()).toInstant()));
            }

            exchange.setResponseCookie(cookie);
        }
    }

    /**
     * Retrieves the flash cookie from the current
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void getFlash(HttpServerExchange exchange) {
        Flash requestFlash = null;
        Cookie cookie = exchange.getRequestCookies().get(this.config.getFlashCookieName());
        if (cookie != null && StringUtils.isNotBlank(cookie.getValue())){
            Map<String, String> values = new HashMap<String, String>();
            for (Map.Entry<String, String> entry : Splitter.on("&").withKeyValueSeparator(":").split(cookie.getValue()).entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }

            requestFlash = new Flash(values);
            requestFlash.setDiscard(true);
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
            String values = Joiner.on("&").withKeyValueSeparator(":").join(this.flash.getValues());

            Cookie cookie = new CookieImpl(this.config.getFlashCookieName(), values)
                    .setSecure(this.config.isFlashCookieSecure())
                    .setHttpOnly(true)
                    .setPath("/");

            exchange.setResponseCookie(cookie);
        } else {
            Cookie cookie = exchange.getRequestCookies().get(this.config.getFlashCookieName());
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
        this.form = this.injector.getInstance(Form.class);
        if (RequestUtils.isPostOrPut(exchange)) {
            Builder builder = FormParserFactory.builder();
            builder.setDefaultCharset(Charsets.UTF_8.name());

            final FormDataParser formDataParser = builder.build().createParser(exchange);
            if (formDataParser != null) {
                exchange.startBlocking();
                FormData formData = formDataParser.parseBlocking();

                for (String data : formData) {
                    for (FormData.FormValue formValue : formData.get(data)) {
                        if (formValue.isFile()) {
                            this.form.addFile(formValue.getFile());
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
        Object [] convertedParameters = new Object[this.parameterCount];

        int index = 0;
        for (Map.Entry<String, Class<?>> entry : this.methodParameters.entrySet()) {
            String key = entry.getKey();
            Class<?> clazz = entry.getValue();
            Binding binding = Optional.ofNullable(Binding.fromString(clazz.getName())).orElse(Binding.UNDEFINED);

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
                convertedParameters[index] = RequestUtils.isJSONRequest(exchange) ? this.opjectMapper.readValue(this.body, clazz) : null;
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
        Map<String, Class<?>> parameters = new LinkedHashMap<String, Class<?>>();
        for (Method declaredMethod : this.controller.getClass().getDeclaredMethods()) {
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
        exchange.setResponseCode(StatusCodes.FOUND);
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
        exchange.setResponseCode(response.getStatusCode());
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
        exchange.startBlocking();
        exchange.setResponseCode(response.getStatusCode());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.toString());
        exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "inline; filename=" + response.getBinaryFileName());
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value)); //NOSONAR
        exchange.getOutputStream().write(response.getBinaryContent());
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
            String noneMatch = exchange.getRequestHeaders().getFirst(Headers.IF_NONE_MATCH_STRING);
            String etag = DigestUtils.md5Hex(responseBody); //NOSONAR
            if (StringUtils.isNotBlank(noneMatch) && StringUtils.isNotBlank(etag) && noneMatch.equals(etag)) {
                exchange.setResponseCode(StatusCodes.NOT_MODIFIED);
                responseBody = "";
            } else {
                exchange.getResponseHeaders().put(Headers.ETAG, etag);
            }
        }

        return responseBody;
    }
}