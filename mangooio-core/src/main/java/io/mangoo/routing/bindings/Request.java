package io.mangoo.routing.bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.utils.JsonUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

/**
 *
 * @author svenkubiak
 *
 */
public class Request extends Validator {
    private static final long serialVersionUID = 1901891944955577394L;
    private transient HttpServerExchange httpServerExchange;
    private transient Session session;
    private transient Authentication authentication;
    private transient Map<String, Cookie> cookies;
    private transient Map<String, Object> attributes = new HashMap<>();
    private String body;
    private String authenticity;
    private Map<String, String> parameter;

    public Request(){
        //Empty constructor for google guice
    }

    public Request(HttpServerExchange httpServerExchange) {
        Objects.requireNonNull(httpServerExchange, Required.HTTP_SERVER_EXCHANGE.toString());

        this.httpServerExchange = httpServerExchange;
        this.cookies = (httpServerExchange.getRequestCookies() == null) ? new HashMap<>() : ImmutableMap.copyOf(httpServerExchange.getRequestCookies());
    }

    public Request withSession(Session session) {
        this.session = session;
        return this;
    }
    
    public Request withAuthenticity(String authenticity) {
        this.authenticity = authenticity;
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
        return this.session;
    }

    /**
     *
     * @return The request body
     */
    public String getBody() {
        return this.body;
    }

    /**
     *
     * @return The request body as Map object
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getBodyAsJsonMap() {
        if (StringUtils.isNotBlank(this.body)) {
            return JsonUtils.fromJson(this.body, Map.class);
        }
        
        return new HashMap<>();
    }

    /**
     *
     * @return The request body as JsonPath object
     */
    public ReadContext getBodyAsJsonPath() {
        return JsonPath.parse(this.body);
    }

    /**
     * Checks if the session bound authenticity token matches the client sent
     * authenticity token
     *
     * @return True if the token matches, false otherwise
     */
    public boolean authenticityMatches() {
        return this.session.getAuthenticity().equals(this.authenticity);
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
     * @param key The key to lookup the parameter
     * @return The value for the given or null if none found
     */
    public String getParameter(String key) {
        return this.parameter.get(key);
    }

    /**
     * Retrieves a map of request parameter (request or query parameter)
     *
     * @return Map of request and query parameter
     */
    public Map<String, String> getParameter() {
        return this.parameter;
    }

    /**
     * Retrieves a list of all headers send by the client
     *
     * @return A HeaderMap of client sent headers
     */
    public HeaderMap getHeaders() {
        return this.httpServerExchange.getRequestHeaders();
    }

    /**
     * Retrieves a the clients accepted languages
     * @author William Dunne
     * @return the string value of the clients accepted languages
     */
    public String getAcceptLanguage() {
        return this.getHeader(Header.ACCEPT_LANGUAGE.toHttpString());
    }
    /**
     * Retrieves a specific header value by its name
     *
     * @param headerName The name of the header to retrieve
     * @return The value of the header or null if none found
     */
    public String getHeader(HttpString headerName) {
        return (this.httpServerExchange.getRequestHeaders().get(headerName) == null) ? null : this.httpServerExchange.getRequestHeaders().get(headerName).element();
    }

    /**
     * The original request URI. This will include the host name, protocol etc
     * if it was specified by the client.
     *
     * This is not decoded in any way, and does not include the query string.
     *
     * Examples:
     * GET http://localhost:8080/myFile.jsf?foo=bar HTTP/1.1 -&gt; 'http://localhost:8080/myFile.jsf'
     * POST /my+File.jsf?foo=bar HTTP/1.1 -&gt; '/my+File.jsf'
     *
     * @return The request URI
     */
    public String getURI() {
        return this.httpServerExchange.getRequestURI();
    }

    /**
     * Reconstructs the complete URL as seen by the user. This includes scheme, host name etc,
     * but does not include query string.
     *
     * This is not decoded.
     *
     * @return The request URL
     */
    public String getURL() {
        return this.httpServerExchange.getRequestURL();
    }

    /**
     * @return An immutable map of request cookies
     */
    public Map<String, Cookie> getCookies() {
        return this.cookies;
    }

    /**
     * Retrieves a single cookie from the request
     *
     * @param name The name of the cookie
     * @return The Cookie
     */
    public Cookie getCookie(String name) {
        return this.cookies.get(name);
    }

    /**
     * Get the request URI scheme.  Normally this is one of {@code http} or {@code https}.
     *
     * @return the request URI scheme
     */
    public String getScheme() {
        return this.httpServerExchange.getRequestScheme();
    }

    /**
     * Returns the request charset. If none was explicitly specified it will return
     * "ISO-8859-1", which is the default charset for HTTP requests.
     *
     * @return The character encoding
     */
    public String getCharset() {
        return this.httpServerExchange.getRequestCharset();
    }
    
    /**
     * Adds an attribute to the internal attributes map
     * 
     * @param key The key to store the attribute
     * @param value The value to store
     */
    public void addAttribute(String key, Object value) {
        Objects.requireNonNull(key, Required.KEY.toString());
        this.attributes.put(key, value);
    }

    /**
     * @return The content length of the request, or <code>-1</code> if it has not been set
     */
    public long getContentLength() {
        return this.httpServerExchange.getRequestContentLength();
    }

    /**
     * Get the HTTP request method.  Normally this is one of the strings listed in {@link io.undertow.util.Methods}.
     *
     * @return the HTTP request method
     */
    public HttpString getMethod() {
        return this.httpServerExchange.getRequestMethod();
    }

    /**
     * The request path. This will be decoded by the server, and does not include the query string.
     *
     * This path is not canonicalised, so care must be taken to ensure that escape attacks are not possible.
     *
     * Examples:
     * GET http://localhost:8080/b/../my+File.jsf?foo=bar HTTP/1.1 -&gt; '/b/../my+File.jsf'
     * POST /my+File.jsf?foo=bar HTTP/1.1 -&gt; '/my File.jsf'
     *
     * @return The request path
     */
    public String getPath() {
        return this.httpServerExchange.getRequestPath();
    }
    
    /**
     * Returns an object attribute from a given key
     * 
     * @param key The key the attribute is stored
     * @return Object the value from the attribues map
     */
    public Object getAttribute(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        return this.attributes.get(key);
    }
    
    /**
     * @return All attributes of the request
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }
}