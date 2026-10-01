package io.mangoo.routing.bindings;

import io.mangoo.constants.Header;
import io.mangoo.constants.Required;
import io.mangoo.utils.JsonUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Request {
    private transient HttpServerExchange httpServerExchange;
    private transient Session session;
    private transient Authentication authentication;
    private transient Map<String, Cookie> cookies = new HashMap<>();
    private transient Map<String, Object> attributes = new HashMap<>();
    private String body = Strings.EMPTY;
    private String csrf = Strings.EMPTY;
    private Map<String, String> parameter;
    private Map<String, String> pathParameter = Map.of();
    private Map<String, String> queryParameter = Map.of();

    public Request(){
        //Empty constructor for Google guice
    }

    public Request(HttpServerExchange httpServerExchange) {
        Objects.requireNonNull(httpServerExchange, Required.HTTP_SERVER_EXCHANGE);

        this.httpServerExchange = httpServerExchange;
        this.httpServerExchange.requestCookies().forEach(cookie -> this.cookies.put(cookie.getName(), cookie));
    }

    public Request withSession(Session session) {
        this.session = session;
        return this;
    }
    
    public Request withAuthentication(Authentication authentication) {
        this.authentication = authentication;
        return this;
    }

    public Request withCsrf(String csrf) {
        this.csrf = csrf;
        return this;
    }
    
    public Request withParameter(Map<String, String> parameter) {
        this.parameter = parameter;
        return this;
    }

    public Request withPathParameter(Map<String, String> pathParameter) {
        this.pathParameter = Map.copyOf(Objects.requireNonNull(pathParameter, Required.PATH_PARAMETER));
        return this;
    }

    public Request withQueryParameter(Map<String, String> queryParameter) {
        this.queryParameter = Map.copyOf(Objects.requireNonNull(queryParameter, Required.QUERY_PARAMETER));
        return this;
    }
    
    public Request withBody(String body) {
        this.body = (body != null) ? body : Strings.EMPTY;
        return this;
    }
    
    public Session getSession() {
        return session;
    }

    public String getBody() {
        return body;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getBodyAsJsonMap() {
        if (StringUtils.isNotBlank(body)) {
            return JsonUtils.toObject(body, Map.class);
        }
        
        return new HashMap<>();
    }

    public boolean hasValidCsrf() {
        String sessionCsrf = session.getCsrf();
        if (StringUtils.isBlank(sessionCsrf) || StringUtils.isBlank(csrf)) {
            return false;
        }

        return MessageDigest.isEqual(
                sessionCsrf.getBytes(StandardCharsets.UTF_8),
                csrf.getBytes(StandardCharsets.UTF_8));
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * A route parameter wins over a query parameter of the same name; never base authorization decisions on this, use getPathParameter instead.
     */
    public String getParameter(String key) {
        return parameter.get(key);
    }

    /**
     * A route parameter wins over a query parameter of the same name.
     */
    public Map<String, String> getParameter() {
        return parameter;
    }

    /**
     * Route parameters are resolved by the router and can not be forged by a client, so only they may be used for authorization decisions.
     */
    public String getPathParameter(String key) {
        return pathParameter.get(key);
    }

    public Map<String, String> getPathParameter() {
        return pathParameter;
    }

    /**
     * Use this instead of a null check on getParameter when the presence of a parameter selects an operation, as a client can add any query parameter.
     */
    public boolean hasPathParameter(String key) {
        return pathParameter.containsKey(key);
    }

    /**
     * Query parameters are always untrusted client input.
     */
    public String getQueryParameter(String key) {
        return queryParameter.get(key);
    }

    public Map<String, String> getQueryParameter() {
        return queryParameter;
    }

    public HeaderMap getHeaders() {
        return httpServerExchange.getRequestHeaders();
    }

    public String getAcceptLanguage() {
        return getHeader(Header.ACCEPT_LANGUAGE);
    }
    
    public String getHeader(HttpString headerName) {
        return Optional.ofNullable(httpServerExchange)
                .map(HttpServerExchange::getRequestHeaders)
                .map(headers -> headers.get(headerName))
                .map(HeaderValues::element)
                .orElse(null);
    }
    
    public String getHeader(String headerName) {
        return getHeader(new HttpString(headerName));
    }    

    /**
     * Not decoded and without the query string; includes host and protocol if the client sent them.
     */
    public String getURI() {
        return httpServerExchange.getRequestURI();
    }

    /**
     * Not decoded and without the query string.
     */
    public String getURL() {
        return httpServerExchange.getRequestURL();
    }

    public Map<String, Cookie> getCookies() {
        return cookies;
    }

    public Cookie getCookie(String name) {
        return cookies.get(name);
    }

    public String getScheme() {
        return httpServerExchange.getRequestScheme();
    }

    /**
     * Defaults to ISO-8859-1 if the client did not specify a charset.
     */
    public String getCharset() {
        return httpServerExchange.getRequestCharset();
    }
    
    public void addAttribute(String key, Object value) {
        Objects.requireNonNull(key, Required.KEY);
        attributes.put(key, value);
    }

    /**
     * Returns -1 if the content length has not been set.
     */
    public long getContentLength() {
        return httpServerExchange.getRequestContentLength();
    }

    public HttpString getMethod() {
        return httpServerExchange.getRequestMethod();
    }

    /**
     * Decoded but not canonical (e.g. may contain ../), so guard against path traversal; does not include the query string.
     */
    public String getPath() {
        return httpServerExchange.getRequestPath();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        Objects.requireNonNull(key, Required.KEY);
        return (T) attributes.get(key);
    }
    
    public String getAttributeAsString(String key) {
        Objects.requireNonNull(key, Required.KEY);
        var object = attributes.get(key);
        
        return object != null ? (String) object : null;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}