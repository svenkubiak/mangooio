package io.mangoo.routing.handlers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.inject.Injector;

import freemarker.template.TemplateException;
import io.mangoo.annotations.FilterWith;
import io.mangoo.authentication.Authentication;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.ContentType;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.enums.Key;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Body;
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
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

public class RequestHandler implements HttpHandler {
    private static final int AUTH_PREFIX_LENGTH = 2;
    private static final int TOKEN_LENGTH = 16;
    private static final int INDEX_2 = 2;
    private static final int INDEX_1 = 1;
    private static final int INDEX_0 = 0;
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
        getRequest(exchange);

        if (continueRequest(exchange)) {
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
    }

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
     * @return True if the request should continue after filter execution, false otherwise
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private boolean continueRequest(HttpServerExchange exchange) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        //execute global request filter if exist
        boolean continueRequest = executeRequestFilter(exchange);

        if (continueRequest) {
            //execute filter on controller level
            continueRequest = executeFilter(this.controllerClass.getAnnotations(), exchange);
        }

        if (continueRequest) {
            //execute filter on method level
            continueRequest = executeFilter(this.method.getAnnotations(), exchange);
        }

        return continueRequest;
    }

    /**
     * Executes a global request filter if exists
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request should continue after filter execution, false otherwise
     */
    private boolean executeRequestFilter(HttpServerExchange exchange) {
        if (this.hasRequestFilter) {
            MangooRequestFilter mangooRequestFilter = this.injector.getInstance(MangooRequestFilter.class);
            return mangooRequestFilter.continueRequest(this.request);
        }

        return true;
    }

    /**
     * Creates a new request object containing the current request data
     *
     * @param exchange The Undertow HttpServerExchange
     */
    private void getRequest(HttpServerExchange exchange) {
        String authenticityToken = this.requestParameter.get(Default.AUTHENTICITY_TOKEN.toString());
        if (StringUtils.isBlank(authenticityToken)) {
            authenticityToken = this.form.get(Default.AUTHENTICITY_TOKEN.toString());
        }

        this.request = new Request(exchange, this.session, authenticityToken, this.authentication, this.requestParameter);
    }

    /**
     * Executes all filters on controller and method level
     *
     * @param annotations An array of @FilterWith annotated classes and methods
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request should continue after filter execution, false otherwise
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private boolean executeFilter(Annotation[] annotations, HttpServerExchange exchange) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        FilterWith filterWith = null;
        boolean continueRequest = true;

        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(FilterWith.class)) {
                filterWith = (FilterWith) annotation;
                for (Class<?> clazz : filterWith.value()) {
                    if (continueRequest) {
                        Method classMethod = clazz.getMethod(Default.FILTER_METHOD.toString(), Request.class);
                        continueRequest = (boolean) classMethod.invoke(this.injector.getInstance(clazz), this.request);
                    } else {
                        return false;
                    }
                }
            }
        }

        return continueRequest;
    }

    /**
     * Invokes the controller methods and retrives the response which
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
    private Response getResponse(HttpServerExchange exchange) throws IllegalAccessException, InvocationTargetException, IOException, TemplateException {
        Response response;

        if (this.methodParameters.isEmpty()) {
            response = (Response) this.method.invoke(this.controller);
            response.andTemplate(this.method.getName());
        } else {
            Object [] convertedParameters = getConvertedParameters(exchange);

            response = (Response) this.method.invoke(this.controller, convertedParameters);
            response.andTemplate(this.method.getName());
        }

        if (!response.isRendered()) {
            response.getContent().putAll(this.request.getPayload().getContent());

            TemplateEngine templateEngine = this.injector.getInstance(TemplateEngine.class);
            response.andBody(templateEngine.render(this.flash, this.session, this.form, this.injector.getInstance(Messages.class), this.controllerClass.getSimpleName(), response.getTemplate(), response.getContent()));
        }

        return response;
    }

    private Session getSession(HttpServerExchange exchange) {
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
                String prefix = StringUtils.substringBefore(cookieValue, Default.DATA_DELIMITER.toString());
                if (StringUtils.isNotBlank(prefix)) {
                    String [] prefixes = prefix.split("\\" + Default.DELIMITER.toString());
                    if (prefixes != null && prefixes.length == SESSION_PREFIX_LENGTH) {
                        sign = prefixes [INDEX_0];
                        authenticityToken = prefixes [INDEX_1];
                        expires = prefixes [INDEX_2];
                    }
                }

                if (StringUtils.isNotBlank(sign) && StringUtils.isNotBlank(expires) && StringUtils.isNotBlank(authenticityToken)) {
                    String data = cookieValue.substring(cookieValue.indexOf(Default.DATA_DELIMITER.toString()) + 1, cookieValue.length());

                    LocalDateTime expiresDate = LocalDateTime.parse(expires);
                    if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(data + authenticityToken + expires + this.config.getApplicationSecret()).equals(sign)) {
                        Map<String, String> sessionValues = new HashMap<String, String>();
                        if (StringUtils.isNotEmpty(data)) {
                            for (Map.Entry<String, String> entry : Splitter.on(Default.SPLITTER.toString()).withKeyValueSeparator(Default.SEPERATOR.toString()).split(data).entrySet()) {
                                sessionValues.put(entry.getKey(), entry.getValue());
                            }
                        }
                        requestSession = new Session(sessionValues);
                        requestSession.setAuthenticityToken(authenticityToken);
                        requestSession.setExpires(expiresDate);
                    }
                }
            }
        }

        if (requestSession == null) {
            requestSession = new Session();
            requestSession.setAuthenticityToken(RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH));
            requestSession.setExpires(LocalDateTime.now().plusSeconds(this.config.getSessionExpires()));
        }

        this.session = requestSession;

        return requestSession;
    }

    private void setSessionCookie(HttpServerExchange exchange) {
        if (this.session != null && this.session.hasChanges()) {
            String values = Joiner.on(Default.SPLITTER.toString()).withKeyValueSeparator(Default.SEPERATOR.toString()).join(this.session.getValues());

            StringBuilder buffer = new StringBuilder()
                    .append(DigestUtils.sha512Hex(values + this.session.getAuthenticityToken() + this.session.getExpires() + this.config.getApplicationSecret()))
                    .append(Default.DELIMITER.toString())
                    .append(this.session.getAuthenticityToken())
                    .append(Default.DELIMITER.toString())
                    .append(this.session.getExpires())
                    .append(Default.DATA_DELIMITER.toString())
                    .append(values);

            String value = buffer.toString();
            if (this.config.getBoolean(Key.COOKIE_ENCRYPTION, false)) {
                Crypto crypto = this.injector.getInstance(Crypto.class);
                value = crypto.encrypt(value);
            }

            Cookie cookie = new CookieImpl(this.config.getString(Key.COOKIE_NAME), value)
                    .setSecure(this.config.isSessionCookieSecure())
                    .setHttpOnly(true)
                    .setPath("/")
                    .setExpires(Date.from(this.session.getExpires().atZone(ZoneId.systemDefault()).toInstant()));

            exchange.setResponseCookie(cookie);
        }
    }

    private Authentication getAuthentication(HttpServerExchange exchange) {
        Authentication requestAuthentication = null;
        Cookie cookie = exchange.getRequestCookies().get(this.config.getAuthenticationCookieName());
        if (cookie != null) {
            String cookieValue = cookie.getValue();
            if (StringUtils.isNotBlank(cookieValue)) {
                if (this.config.getBoolean(Key.AUTH_COOKIE_ENCRYPT.toString(), false)) {
                    Crypto crypto = this.injector.getInstance(Crypto.class);
                    cookieValue = crypto.decrypt(cookieValue);
                }

                String sign = null;
                String expires = null;
                String prefix = StringUtils.substringBefore(cookieValue, Default.DATA_DELIMITER.toString());
                if (StringUtils.isNotBlank(prefix)) {
                    String [] prefixes = prefix.split("\\" + Default.DELIMITER.toString());
                    if (prefixes != null && prefixes.length == AUTH_PREFIX_LENGTH) {
                        sign = prefixes [INDEX_0];
                        expires = prefixes [INDEX_1];
                    }
                }

                if (StringUtils.isNotBlank(sign) && StringUtils.isNotBlank(expires)) {
                    String data = cookieValue.substring(cookieValue.indexOf(Default.DATA_DELIMITER.toString()) + 1, cookieValue.length());
                    LocalDateTime expiresDate = LocalDateTime.parse(expires);
                    if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(data + expires + this.config.getApplicationSecret()).equals(sign)) {
                        requestAuthentication = new Authentication(this.config, data, expiresDate);
                    }
                }
            }
        }

        if (requestAuthentication == null) {
            requestAuthentication = new Authentication(this.config);
            requestAuthentication.setExpires(LocalDateTime.now().plusSeconds(this.config.getAuthenticationExpires()));
        }

        this.authentication = requestAuthentication;

        return requestAuthentication;
    }

    private void setAuthenticationCookie(HttpServerExchange exchange) {
        if (this.authentication != null && this.authentication.hasAuthenticatedUser()) {
            Cookie cookie;
            String cookieName = this.config.getAuthenticationCookieName();
            if (this.authentication.isLogout()) {
                cookie = exchange.getRequestCookies().get(cookieName);
                cookie.setSecure(this.config.getAuthenticationCookieSecure());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                cookie.setDiscard(true);
            } else {
                StringBuilder buffer = new StringBuilder()
                        .append(DigestUtils.sha512Hex(this.authentication.getAuthenticatedUser() + this.authentication.getExpires() + this.config.getApplicationSecret()))
                        .append(Default.DELIMITER.toString())
                        .append(this.authentication.getExpires())
                        .append(Default.DATA_DELIMITER.toString())
                        .append(this.authentication.getAuthenticatedUser());

                String value = buffer.toString();
                if (this.config.getBoolean(Key.AUTH_COOKIE_ENCRYPT, false)) {
                    value = this.injector.getInstance(Crypto.class).encrypt(value);
                }

                cookie = new CookieImpl(cookieName, value)
                        .setSecure(this.config.getAuthenticationCookieSecure())
                        .setHttpOnly(true)
                        .setPath("/")
                        .setExpires(Date.from(this.authentication.getExpires().atZone(ZoneId.systemDefault()).toInstant()));
            }

            exchange.setResponseCookie(cookie);
        }
    }

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

    private void getForm(HttpServerExchange exchange) throws IOException {
        this.form = this.injector.getInstance(Form.class);
        if (RequestUtils.isPostOrPut(exchange)) {
            final FormDataParser formDataParser = FormParserFactory.builder().build().createParser(exchange);
            if (formDataParser != null) {
                exchange.startBlocking();
                FormData formData = formDataParser.parseBlocking();

                for (String data : formData) {
                    for (FormData.FormValue formValue : formData.get(data)) {
                        if (formValue.isFile()) {
                            form.addFile(formValue.getFile());
                        } else {
                            form.add(new HttpString(data).toString(), formValue.getValue());
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
    private Body getBody(HttpServerExchange exchange) throws IOException {
        Body body = new Body();
        if (RequestUtils.isPostOrPut(exchange)) {
            exchange.startBlocking();
            body.setContent(IOUtils.toString(exchange.getInputStream()));
        }

        return body;
    }

    private Object[] getConvertedParameters(HttpServerExchange exchange) throws IOException {
        Object [] convertedParameters = new Object[this.parameterCount];

        int index = 0;
        for (Map.Entry<String, Class<?>> entry : this.methodParameters.entrySet()) {
            String key = entry.getKey();
            Class<?> clazz = entry.getValue();

            if ((Form.class).equals(clazz)) {
                convertedParameters[index] = this.form;
            } else if ((Authentication.class).equals(clazz)) {
                convertedParameters[index] = this.authentication;
            } else if ((Session.class).equals(clazz)) {
                convertedParameters[index] = this.session;
            } else if ((Flash.class).equals(clazz)) {
                convertedParameters[index] = this.flash;
            } else if ((Request.class).equals(clazz)) {
                convertedParameters[index] = this.request;
            }else if ((Body.class).equals(clazz)) {
                convertedParameters[index] = getBody(exchange);
            } else if ((LocalDate.class).equals(clazz)) {
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? "" : LocalDate.parse(this.requestParameter.get(key));
            } else if ((LocalDateTime.class).equals(clazz)) {
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? "" : LocalDateTime.parse(this.requestParameter.get(key));
            } else if ((String.class).equals(clazz)) {
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? "" : this.requestParameter.get(key);
            } else if ((Integer.class).equals(clazz) || (int.class).equals(clazz)) {
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? Integer.valueOf(0) : Integer.valueOf(this.requestParameter.get(key));
            } else if ((Double.class).equals(clazz) || (double.class).equals(clazz)) {
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? Double.valueOf(0) : Double.valueOf(this.requestParameter.get(key));
            } else if ((Float.class).equals(clazz) || (float.class).equals(clazz)) {
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? Float.valueOf(0) : Float.valueOf(this.requestParameter.get(key));
            } else if ((Long.class).equals(clazz) || (long.class).equals(clazz)) {
                convertedParameters[index] = StringUtils.isBlank(this.requestParameter.get(key)) ? Long.valueOf(0) : Long.valueOf(this.requestParameter.get(key));
            } else if (exchange.getRequestHeaders() != null && exchange.getRequestHeaders().get(Headers.CONTENT_TYPE) != null &&
                    (ContentType.APPLICATION_JSON.toString()).equals(exchange.getRequestHeaders().get(Headers.CONTENT_TYPE).element())) {
                convertedParameters[index] = this.opjectMapper.readValue(getBody(exchange).asString(), clazz);
            }

            index++;
        }

        return convertedParameters;
    }

    private Map<String, Class<?>> getMethodParameters() {
        Map<String, Class<?>> methodParameters = new LinkedHashMap<String, Class<?>>();
        for (Method declaredMethod : this.controller.getClass().getDeclaredMethods()) {
            if (declaredMethod.getName().equals(this.controllerMethod) && declaredMethod.getParameterCount() > 0) {
                Parameter[] declaredParameters = declaredMethod.getParameters();
                for (Parameter parameter : declaredParameters) {
                    methodParameters.put(parameter.getName(), parameter.getType());
                }
                break;
            }
        }

        return methodParameters;
    }

    private void handleRedirectResponse(HttpServerExchange exchange, Response response) {
        exchange.setResponseCode(StatusCodes.FOUND);
        exchange.getResponseHeaders().put(Headers.LOCATION, response.getRedirectTo());
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        exchange.endExchange();
    }

    private void handleRenderedResponse(HttpServerExchange exchange, Response response) {
        exchange.setResponseCode(response.getStatusCode());
        exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), Default.X_XSS_PPROTECTION.toInt());
        exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), Default.NOSNIFF.toString());
        exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), Default.SAMEORIGIN.toString());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, response.getContentType() + "; charset=" + response.getCharset());
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());

        if (response.isETag()) {
            exchange.getResponseHeaders().put(Headers.ETAG, DigestUtils.md5Hex(response.getBody()));
        }

        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.getResponseSender().send(response.getBody());
    }

    private void handleBinaryResponse(HttpServerExchange exchange, Response response) {
        exchange.dispatch(exchange.getDispatchExecutor(), new BinaryHandler(response));
    }
}