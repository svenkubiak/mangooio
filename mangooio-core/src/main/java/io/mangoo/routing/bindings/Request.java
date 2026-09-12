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
    
    /**
     * @return The current session
     */
    public Session getSession() {
        return session;
    }

    /**
     *
     * @return The request body
     */
    public String getBody() {
        return body;
    }

    /**
     *
     * @return The request body as Map object
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getBodyAsJsonMap() {
        if (StringUtils.isNotBlank(body)) {
            return JsonUtils.toObject(body, Map.class);
        }
        
        return new HashMap<>();
    }

    /**
     * Checks if the session bound authenticity token matches the client sent
     * authenticity token
     *
     * @return True if the token matches, false otherwise
     */
    public boolean hasValidCsrf() {
        String sessionCsrf = session.getCsrf();
        if (StringUtils.isBlank(sessionCsrf) || StringUtils.isBlank(csrf)) {
            return false;
        }

        return MessageDigest.isEqual(
                sessionCsrf.getBytes(StandardCharsets.UTF_8),
                csrf.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @return The current authentication
     */
    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * Retrieves a merged view of route and query parameter by its name, where a route parameter
     * always wins over a query parameter of the same name
     * <p>
     * Authorization decisions must not be based on this method, as it can not tell a route
     * parameter apart from client sent input. Use {@link #getPathParameter(String)} for that and
     * {@link #getQueryParameter(String)} for client sent input.
     *
     * @param key The key to find the parameter
     * @return The value for the given or null if none found
     */
    public String getParameter(String key) {
        return parameter.get(key);
    }

    /**
     * Retrieves a merged map of route and query parameter, where a route parameter always wins
     * over a query parameter of the same name
     *
     * @return Map of route and query parameter
     */
    public Map<String, String> getParameter() {
        return parameter;
    }

    /**
     * Retrieves a route parameter by its name, e.g. {@code id} of a route {@code /users/{id}}
     * <p>
     * Route parameters are resolved by the router and can not be forged by a client, which makes
     * them the only parameters an authorization decision may be based on.
     *
     * @param key The key to find the route parameter
     * @return The value for the given key or null if the route declares no such parameter
     */
    public String getPathParameter(String key) {
        return pathParameter.get(key);
    }

    /**
     * @return Map of all route parameters of the matched route
     */
    public Map<String, String> getPathParameter() {
        return pathParameter;
    }

    /**
     * Checks if the matched route declares a parameter with the given name
     * <p>
     * Use this instead of a null check on {@link #getParameter(String)} when the presence of a
     * parameter selects an operation, as a client can always add a query parameter of any name.
     *
     * @param key The key to look up
     * @return True if the matched route declares a parameter with this name, false otherwise
     */
    public boolean hasPathParameter(String key) {
        return pathParameter.containsKey(key);
    }

    /**
     * Retrieves a query parameter by its name, e.g. {@code limit} of {@code ?limit=25}
     * <p>
     * Query parameters are always untrusted client input.
     *
     * @param key The key to find the query parameter
     * @return The value for the given key or null if none found
     */
    public String getQueryParameter(String key) {
        return queryParameter.get(key);
    }

    /**
     * @return Map of all query parameters of the request
     */
    public Map<String, String> getQueryParameter() {
        return queryParameter;
    }

    /**
     * Retrieves a list of all headers send by the client
     *
     * @return A HeaderMap of client sent headers
     */
    public HeaderMap getHeaders() {
        return httpServerExchange.getRequestHeaders();
    }

    /**
     * Retrieves the clients accepted languages
     * @return the string value of the clients accepted languages
     */
    public String getAcceptLanguage() {
        return getHeader(Header.ACCEPT_LANGUAGE);
    }
    
    /**
     * Retrieves a specific header value by its name
     *
     * @param headerName The name of the header to retrieve
     * @return The value of the header or null if none found
     */
    public String getHeader(HttpString headerName) {
        return Optional.ofNullable(httpServerExchange)
                .map(HttpServerExchange::getRequestHeaders)
                .map(headers -> headers.get(headerName))
                .map(HeaderValues::element)
                .orElse(null);
    }
    
    /**
     * Retrieves a specific header value by its name
     *
     * @param headerName The name of the header to retrieve
     * @return The value of the header or null if none found
     */
    public String getHeader(String headerName) {
        return getHeader(new HttpString(headerName));
    }    

    /**
     * The original request URI. This will include the host name, protocol etc.
     * if it was specified by the client.
     * <p></p>
     * This is not decoded in any way, and does not include the query string.
     * <p></p>
     * Examples:
     * GET http://localhost:8080/myFile.jsf?foo=bar HTTP/1.1 -&gt; 'http://localhost:8080/myFile.jsf'
     * POST /my+File.jsf?foo=bar HTTP/1.1 -&gt; '/my+File.jsf'
     *
     * @return The request URI
     */
    public String getURI() {
        return httpServerExchange.getRequestURI();
    }

    /**
     * Reconstructs the complete URL as seen by the user. This includes scheme, host name etc.
     * but does not include query string.
     * <p></p>
     * This is not decoded.
     *
     * @return The request URL
     */
    public String getURL() {
        return httpServerExchange.getRequestURL();
    }

    /**
     * @return An immutable map of request cookies
     */
    public Map<String, Cookie> getCookies() {
        return cookies;
    }

    /**
     * Retrieves a single cookie from the request
     *
     * @param name The name of the cookie
     * @return The Cookie
     */
    public Cookie getCookie(String name) {
        return cookies.get(name);
    }

    /**
     * Get the request URI scheme.  Normally this is one of {@code http} or {@code https}.
     *
     * @return the request URI scheme
     */
    public String getScheme() {
        return httpServerExchange.getRequestScheme();
    }

    /**
     * Returns the request charset. If none was explicitly specified it will return
     * "ISO-8859-1", which is the default charset for HTTP requests.
     *
     * @return The character encoding
     */
    public String getCharset() {
        return httpServerExchange.getRequestCharset();
    }
    
    /**
     * Adds an attribute to the internal attributes map
     * 
     * @param key The key to store the attribute
     * @param value The value to store
     */
    public void addAttribute(String key, Object value) {
        Objects.requireNonNull(key, Required.KEY);
        attributes.put(key, value);
    }

    /**
     * @return The content length of the request, or <code>-1</code> if it has not been set
     */
    public long getContentLength() {
        return httpServerExchange.getRequestContentLength();
    }

    /**
     * Get the HTTP request method.  Normally this is one of the strings listed in {@link io.undertow.util.Methods}.
     *
     * @return the HTTP request method
     */
    public HttpString getMethod() {
        return httpServerExchange.getRequestMethod();
    }

    /**
     * The request path. This will be decoded by the server, and does not include the query string.
     * <p></p>
     * This path is not canonical, so care must be taken to ensure that escape attacks are not possible.
     * <p></p>
     * Examples:
     * GET http://localhost:8080/b/../my+File.jsf?foo=bar HTTP/1.1 -&gt; '/b/../my+File.jsf'
     * POST /my+File.jsf?foo=bar HTTP/1.1 -&gt; '/my File.jsf'
     *
     * @return The request path
     */
    public String getPath() {
        return httpServerExchange.getRequestPath();
    }
    
    /**
     * Returns an object attribute from a given key
     * 
     * @param key The key the attribute is stored
     * @return Object the value from the attributes map
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        Objects.requireNonNull(key, Required.KEY);
        return (T) attributes.get(key);
    }
    
    /**
     * Returns an object attribute from a given key
     * 
     * @param key The key the attribute is stored
     * @return String the value from the attributes map
     */
    public String getAttributeAsString(String key) {
        Objects.requireNonNull(key, Required.KEY);
        var object = attributes.get(key);
        
        return object != null ? (String) object : null;
    }

    /**
     * @return All attributes of the request
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}