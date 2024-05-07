package io.mangoo.routing.bindings;

import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.mangoo.utils.JsonUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Request extends Validator {
    @Serial
    private static final long serialVersionUID = 5589488716007844048L;
    private transient HttpServerExchange httpServerExchange;
    private transient Session session;
    private transient Authentication authentication;
    private transient Map<String, Cookie> cookies = new HashMap<>();
    private transient Map<String, Object> attributes = new HashMap<>();
    private String body;
    private Map<String, String> parameter;

    public Request(){
        //Empty constructor for Google guice
    }

    public Request(HttpServerExchange httpServerExchange) {
        Objects.requireNonNull(httpServerExchange, NotNull.HTTP_SERVER_EXCHANGE);

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
    
    public Request withParameter(Map<String, String> parameter) {
        this.parameter = parameter;
        this.setValues(this.parameter);
        return this;
    }
    
    public Request withBody(String body) {
        this.body = body;
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
     * @return The current authentication
     */
    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * Retrieves a request parameter (request or query parameter) by its name
     *
     * @param key The key to find the parameter
     * @return The value for the given or null if none found
     */
    public String getParameter(String key) {
        return parameter.get(key);
    }

    /**
     * Retrieves a map of request parameter (request or query parameter)
     *
     * @return Map of request and query parameter
     */
    public Map<String, String> getParameter() {
        return parameter;
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
        return (httpServerExchange.getRequestHeaders().get(headerName) == null) ? null : httpServerExchange.getRequestHeaders().get(headerName).element();
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
        Objects.requireNonNull(key, NotNull.KEY);
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
        Objects.requireNonNull(key, NotNull.KEY);
        return (T) attributes.get(key);
    }
    
    /**
     * Returns an object attribute from a given key
     * 
     * @param key The key the attribute is stored
     * @return String the value from the attributes map
     */
    public String getAttributeAsString(String key) {
        Objects.requireNonNull(key, NotNull.KEY);
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