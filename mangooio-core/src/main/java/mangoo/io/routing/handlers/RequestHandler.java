package mangoo.io.routing.handlers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.inject.Injector;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import mangoo.io.annotations.FilterWith;
import mangoo.io.authentication.Authentication;
import mangoo.io.configuration.Config;
import mangoo.io.core.Application;
import mangoo.io.crypto.Crypto;
import mangoo.io.enums.ContentType;
import mangoo.io.enums.Default;
import mangoo.io.enums.Key;
import mangoo.io.i18n.Messages;
import mangoo.io.interfaces.MangooGlobalFilter;
import mangoo.io.routing.Response;
import mangoo.io.routing.bindings.Body;
import mangoo.io.routing.bindings.Exchange;
import mangoo.io.routing.bindings.Flash;
import mangoo.io.routing.bindings.Form;
import mangoo.io.routing.bindings.Session;
import mangoo.io.templating.TemplateEngine;

/**
 *
 * @author svenkubiak
 *
 */
public class RequestHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RequestHandler.class);
    private static final AttachmentKey<Throwable> THROWABLE = AttachmentKey.create(Throwable.class);
    private int parameterCount;
    private Class<?> controllerClass;
    private String controllerMethod;
    private Object controller;
    private Map<String, Class<?>> methodParameters;
    private Method method;
    private ObjectMapper mapper;
    private Authentication authentication;
    private Session session;
    private Flash flash;
    private Form form;
    private Config config;
    private Injector injector;
    private Exchange exchange;
    private boolean globalFilter;

    public RequestHandler(Class<?> controllerClass, String controllerMethod, Injector injector) {
        this.injector = injector;
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
        this.controller = this.injector.getInstance(this.controllerClass);
        this.methodParameters = getMethodParameters();
        this.parameterCount = this.methodParameters.size();
        this.config = this.injector.getInstance(Config.class);
        this.globalFilter = this.injector.getAllBindings().containsKey(com.google.inject.Key.get(MangooGlobalFilter.class));
    }

    @Override
    @SuppressWarnings("all")
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            if (this.method == null) {
                this.method = this.controller.getClass().getMethod(this.controllerMethod, methodParameters.values().toArray(new Class[0]));
            }
            this.exchange = null;

            setLocale(exchange);
            getSession(exchange);
            getAuthentication(exchange);
            getFlash(exchange);
            getForm(exchange);

            boolean continueAfterFilter = executeFilter(exchange);
            if (continueAfterFilter) {
                Response response = getResponse(exchange);

                setSession(exchange);
                setFlash(exchange);
                setAuthentication(exchange);

                if (response.isRedirect()) {
                    exchange.setResponseCode(StatusCodes.FOUND);
                    exchange.getResponseHeaders().put(Headers.LOCATION, response.getRedirectTo());
                    exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
                    exchange.endExchange();
                } else if (response.isBinary()) {
                    exchange.dispatch(exchange.getDispatchExecutor(), new BinaryHandler(response));
                } else {
                    exchange.setResponseCode(response.getStatusCode());
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, response.getContentType() + "; charset=" + response.getCharset());
                    exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
                    response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
                    exchange.getResponseSender().send(response.getBody());
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to handle request", e);
            exchange.putAttachment(THROWABLE, e);
            throw new Exception();
        }
    }

    private void setLocale(HttpServerExchange exchange) throws Exception {
        HeaderValues headerValues = exchange.getRequestHeaders().get(Headers.ACCEPT_LANGUAGE_STRING);
        if (headerValues != null && headerValues.getFirst() != null) {
            Iterable<String> split = Splitter.on(",").trimResults().split(headerValues.getFirst());
            if (split != null) {
                String language = split.iterator().next();
                if (StringUtils.isBlank(language)) {
                    language = this.config.getString(Key.APPLICATION_LANGUAGE, Default.LANGUAGE.toString());
                }

                Locale.setDefault(Locale.forLanguageTag(language.substring(0, 1)));
                Application.getInjector().getInstance(Messages.class).reload();
            }
        }
    }

    private boolean executeFilter(HttpServerExchange exchange) throws Exception {
        boolean continueAfterFilter = executeGlobalFilter(exchange);

        if (continueAfterFilter) {
            continueAfterFilter = executeFilter(this.controllerClass.getAnnotations(), exchange);
        }

        if (continueAfterFilter) {
            continueAfterFilter = executeFilter(this.method.getAnnotations(), exchange);
        }

        return continueAfterFilter;
    }

    private boolean executeGlobalFilter(HttpServerExchange exchange) throws Exception {
        if (this.globalFilter) {
            MangooGlobalFilter mangooGlobalFilter = this.injector.getInstance(MangooGlobalFilter.class);
            return mangooGlobalFilter.filter(getExchange(exchange));
        }

        return true;
    }

    private Exchange getExchange(HttpServerExchange httpServerExchange) throws Exception {
        if (this.exchange == null) {
            String authenticityToken = getRequestParameters(httpServerExchange).get(Default.AUTHENTICITY_TOKEN.toString());
            if (StringUtils.isBlank(authenticityToken)) {
                authenticityToken = this.form.get(Default.AUTHENTICITY_TOKEN.toString());
            }

            this.exchange = new Exchange(httpServerExchange, this.session, authenticityToken, this.authentication);
        }

        return this.exchange;
    }

    private boolean executeFilter(Annotation[] annotations, HttpServerExchange exchange) throws Exception {
        FilterWith filterWith = null;
        boolean continueAfterFilter = true;

        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(FilterWith.class)) {
                filterWith = (FilterWith) annotation;
                for (Class<?> clazz : filterWith.value()) {
                    if (continueAfterFilter) {
                        Method method = clazz.getMethod(Default.FILTER_METHOD_NAME.toString(), Exchange.class);
                        continueAfterFilter = (boolean) method.invoke(this.injector.getInstance(clazz), getExchange(exchange));
                    } else {
                        return false;
                    }
                }
            }
        }

        return continueAfterFilter;
    }

    private Response getResponse(HttpServerExchange exchange) throws Exception {
        Response response;

        if (this.methodParameters.isEmpty()) {
            response = (Response) this.method.invoke(this.controller);
            response.andTemplate(this.method.getName());
        } else {
            Object [] methodParameters = getConvertedParameters(exchange);

            response = (Response) this.method.invoke(this.controller, methodParameters);
            response.andTemplate(this.method.getName());
        }

        if (!response.isRendered()) {
            if (response.getContent() != null && this.exchange != null && this.exchange.getContent() != null) {
                response.getContent().putAll(this.exchange.getContent());
            }

            TemplateEngine templateEngine = this.injector.getInstance(TemplateEngine.class);
            response.andBody(templateEngine.render(this.flash, this.session, this.form, this.injector.getInstance(Messages.class), this.controllerClass.getSimpleName(), response.getTemplate(), response.getContent()));
        }

        return response;
    }

    private Session getSession(HttpServerExchange exchange) throws Exception {
        Session session = null;
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
                    if (prefixes != null && prefixes.length == 3) {
                        sign = prefixes [0];
                        authenticityToken = prefixes [1];
                        expires = prefixes [2];
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
                        session = new Session(sessionValues);
                        session.setAuthenticityToken(authenticityToken);
                        session.setExpires(expiresDate);
                    }
                }
            }
        }

        if (session == null) {
            session = new Session();
            session.setAuthenticityToken(RandomStringUtils.randomAlphanumeric(16));
            session.setExpires(LocalDateTime.now().plusSeconds(this.config.getSessionExpires()));
        }

        this.session = session;

        return session;
    }

    private void setSession(HttpServerExchange exchange) throws Exception {
        if (this.session != null && this.session.hasChanges()) {
            String values = Joiner.on(Default.SPLITTER.toString()).withKeyValueSeparator(Default.SEPERATOR.toString()).join(this.session.getValues());

            String sign = DigestUtils.sha512Hex(values + session.getAuthenticityToken() + session.getExpires() + config.getString(Key.APPLICATION_SECRET));
            String value = sign + Default.DELIMITER.toString() + this.session.getAuthenticityToken() + Default.DELIMITER.toString() + this.session.getExpires() + Default.DATA_DELIMITER.toString() + values;

            if (this.config.getBoolean(Key.COOKIE_ENCRYPTION, false)) {
                Crypto crypto = this.injector.getInstance(Crypto.class);
                value = crypto.encrypt(value);
            }

            Cookie cookie = new CookieImpl(config.getString(Key.COOKIE_NAME), value)
                    .setHttpOnly(true)
                    .setPath("/")
                    .setExpires(Date.from(this.session.getExpires().atZone(ZoneId.systemDefault()).toInstant()));

            exchange.setResponseCookie(cookie);
            this.session = null;
        }
    }

    private Authentication getAuthentication(HttpServerExchange exchange) {
        Authentication authentication = null;
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
                    if (prefixes != null && prefixes.length == 2) {
                        sign = prefixes [0];
                        expires = prefixes [1];
                    }
                }

                if (StringUtils.isNotBlank(sign) && StringUtils.isNotBlank(expires)) {
                    String data = cookieValue.substring(cookieValue.indexOf(Default.DATA_DELIMITER.toString()) + 1, cookieValue.length());
                    LocalDateTime expiresDate = LocalDateTime.parse(expires);
                    if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(data + expires + this.config.getApplicationSecret()).equals(sign)) {
                        authentication = new Authentication(this.config, data, expiresDate);
                    }
                }
            }
        }

        if (authentication == null) {
            authentication = new Authentication(this.config);
            authentication.setExpires(LocalDateTime.now().plusSeconds(this.config.getAuthenticationExpires()));
        }

        this.authentication = authentication;

        return authentication;
    }

    private void setAuthentication(HttpServerExchange exchange) {
        if (this.authentication != null && this.authentication.hasAuthenticatedUser()) {
            Cookie cookie;
            String cookieName = this.config.getAuthenticationCookieName();
            if (this.authentication.isLogout()) {
                cookie = exchange.getRequestCookies().get(cookieName);
                cookie.setMaxAge(0);
                cookie.setDiscard(true);
            } else {
                String sign = DigestUtils.sha512Hex(this.authentication.getAuthenticatedUser() + this.authentication.getExpires() + this.config.getString(Key.APPLICATION_SECRET));
                String value = sign + Default.DELIMITER.toString() + this.authentication.getExpires() + Default.DATA_DELIMITER.toString() + this.authentication.getAuthenticatedUser();

                if (this.config.getBoolean(Key.AUTH_COOKIE_ENCRYPT, false)) {
                    value = this.injector.getInstance(Crypto.class).encrypt(value);
                }

                cookie = new CookieImpl(cookieName, value)
                        .setHttpOnly(true)
                        .setPath("/")
                        .setExpires(Date.from(this.authentication.getExpires().atZone(ZoneId.systemDefault()).toInstant()));
            }

            exchange.setResponseCookie(cookie);
            this.authentication = null;
        }
    }

    private void getFlash(HttpServerExchange exchange) throws Exception {
        Flash flash = null;
        Cookie cookie = exchange.getRequestCookies().get(this.config.getFlashCookieName());
        if (cookie != null && StringUtils.isNotBlank(cookie.getValue())){
            Map<String, String> values = new HashMap<String, String>();
            for (Map.Entry<String, String> entry : Splitter.on("&").withKeyValueSeparator(":").split(cookie.getValue()).entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }

            flash = new Flash(values);
            flash.setDiscard(true);
        }

        if (flash == null) {
            flash = new Flash();
        }

        this.flash = flash;
    }

    private void setFlash(HttpServerExchange exchange) throws Exception {
        if (this.flash != null && !this.flash.isDiscard() && this.flash.hasContent()) {
            String values = Joiner.on("&").withKeyValueSeparator(":").join(this.flash.getValues());

            Cookie cookie = new CookieImpl(this.config.getFlashCookieName(), values)
                    .setHttpOnly(true)
                    .setPath("/");

            exchange.setResponseCookie(cookie);
        } else {
            Cookie cookie = exchange.getRequestCookies().get(this.config.getFlashCookieName());
            if (cookie != null) {
                cookie.setHttpOnly(true)
                .setPath("/")
                .setMaxAge(0);

                exchange.setResponseCookie(cookie);
            }
        }
        this.flash = null;
    }

    private void getForm(HttpServerExchange exchange) throws IOException {
        this.form = this.injector.getInstance(Form.class);
        if (exchange.getRequestMethod().equals(Methods.POST) || exchange.getRequestMethod().equals(Methods.PUT)) {
            final FormDataParser formDataParser = FormParserFactory.builder().build().createParser(exchange);
            if (formDataParser != null) {
                exchange.startBlocking();
                FormData formData = formDataParser.parseBlocking();

                for (String data : formData) {
                    for (FormData.FormValue value : formData.get(data)) {
                        if (value.isFile()) {
                            form.addFile(value.getFile());
                        } else {
                            form.add(new HttpString(data).toString(), value.getValue());
                        }
                    }
                }

                this.form.setSubmitted(true);
            }
        }
    }

    private Body getBody(HttpServerExchange exchange) throws IOException {
        Body body = new Body();
        if (exchange.getRequestMethod().equals(Methods.POST) || exchange.getRequestMethod().equals(Methods.PUT)) {
            exchange.startBlocking();
            body.setContent(IOUtils.toString(exchange.getInputStream()));
        }

        return body;
    }

    private Object[] getConvertedParameters(HttpServerExchange exchange) throws Exception {
        Map<String, String> queryParameters = getRequestParameters(exchange);
        Object [] parameters = new Object[this.parameterCount];

        int index = 0;
        for (Map.Entry<String, Class<?>> entry : this.methodParameters.entrySet()) {
            String key = entry.getKey();
            Class<?> clazz = entry.getValue();

            if ((Form.class).equals(clazz)) {
                parameters[index] = this.form;
            } else if ((Body.class).equals(clazz)) {
                parameters[index] = getBody(exchange);
            } else if ((Authentication.class).equals(clazz)) {
                parameters[index] = this.authentication;
            } else if ((Session.class).equals(clazz)) {
                parameters[index] = this.session;
            } else if ((Flash.class).equals(clazz)) {
                parameters[index] = this.flash;
            } else if ((String.class).equals(clazz)) {
                parameters[index] = (StringUtils.isBlank(queryParameters.get(key))) ? "" : queryParameters.get(key);
            } else if ((Integer.class).equals(clazz)) {
                parameters[index] = (StringUtils.isBlank(queryParameters.get(key))) ? Integer.valueOf(0) : Integer.valueOf(queryParameters.get(key));
            } else if ((int.class).equals(clazz)) {
                parameters[index] = (StringUtils.isBlank(queryParameters.get(key))) ? Integer.valueOf(0) : Integer.valueOf(queryParameters.get(key));
            } else if ((Double.class).equals(clazz)) {
                parameters[index] = (StringUtils.isBlank(queryParameters.get(key))) ? Double.valueOf(0) : Double.valueOf(queryParameters.get(key));
            } else if ((double.class).equals(clazz)) {
                parameters[index] = (StringUtils.isBlank(queryParameters.get(key))) ? Double.valueOf(0) : Double.valueOf(queryParameters.get(key));
            } else if ((Float.class).equals(clazz)) {
                parameters[index] = (StringUtils.isBlank(queryParameters.get(key))) ? Float.valueOf(0) : Float.valueOf(queryParameters.get(key));
            } else if ((float.class).equals(clazz)) {
                parameters[index] = (StringUtils.isBlank(queryParameters.get(key))) ? Float.valueOf(0) : Float.valueOf(queryParameters.get(key));
            } else if ((Long.class).equals(clazz)) {
                parameters[index] = (StringUtils.isBlank(queryParameters.get(key))) ? Long.valueOf(0) : Long.valueOf(queryParameters.get(key));
            } else if ((long.class).equals(clazz)) {
                parameters[index] = (StringUtils.isBlank(queryParameters.get(key))) ? Long.valueOf(0) : Long.valueOf(queryParameters.get(key));
            } else if ((ContentType.APPLICATION_JSON.toString()).equals(exchange.getRequestHeaders().get(Headers.CONTENT_TYPE).element())) {
                if (this.mapper == null) {
                    this.mapper = JsonFactory.create();
                }
                parameters[index] = this.mapper.readValue(getBody(exchange).asString(), clazz);
            }

            index++;
        }

        return parameters;
    }

    private Map<String, String> getRequestParameters(HttpServerExchange exchange) throws Exception {
        Map<String, String> requestParamater = new HashMap<String, String>();

        Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        queryParameters.putAll(exchange.getPathParameters());

        for (Map.Entry<String, Deque<String>> entry : queryParameters.entrySet()) {
            requestParamater.put(entry.getKey(), entry.getValue().element());
        }

        return (requestParamater == null) ? new HashMap<String, String>() : requestParamater;
    }

    private Map<String, Class<?>> getMethodParameters() {
        Map<String, Class<?>> methodParameters = new LinkedHashMap<String, Class<?>>();
        for (Method method : this.controller.getClass().getDeclaredMethods()) {
            if (method.getName().equals(this.controllerMethod) && method.getParameterCount() > 0) {
                Parameter[] parameters = method.getParameters();
                for (Parameter parameter : parameters) {
                    methodParameters.put(parameter.getName(), parameter.getType());
                }
                break;
            }
        }

        return methodParameters;
    }
}