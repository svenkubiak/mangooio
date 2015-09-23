package io.mangoo.routing.bindings;

import java.util.Map;

import org.boon.json.JsonFactory;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.mangoo.authentication.Authentication;
import io.mangoo.core.Application;
import io.mangoo.interfaces.MangooValidator;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

/**
 *
 * @author svenkubiak
 *
 */
public class Request implements MangooValidator {
    private HttpServerExchange httpServerExchange;
    private String body;
    private Session session;
    private String authenticityToken;
    private Authentication authentication;
    private Validator validator;
    private Map<String, String> parameter;

    public Request(HttpServerExchange httpServerExchange, Session session, String authenticityToken, Authentication authentication, Map<String, String> parameter, String body) {
        this.httpServerExchange = httpServerExchange;
        this.session = session;
        this.authenticityToken = authenticityToken;
        this.authentication = authentication;
        this.body = body;
        this.validator = Application.getInjector().getInstance(Validator.class);
        this.parameter = parameter;
        this.validator.setValues(parameter);
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
        return JsonFactory.create().readValue(this.body, Map.class);
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
        return this.session.getAuthenticityToken().equals(this.authenticityToken);
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

    @Override
    public Validator validation() {
        return this.validator;
    }

    @Override
    public String getError(String name) {
        return this.validator.hasError(name) ? this.validator.getError(name) : "";
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
     */
    public String getURI() {
        return this.httpServerExchange.getRequestURI();
    }

    /**
     * Reconstructs the complete URL as seen by the user. This includes scheme, host name etc,
     * but does not include query string.
     *
     * This is not decoded.
     */
    public String getURL() {
        return this.httpServerExchange.getRequestURL();
    }

    /**
     * @return A mutable map of request cookies
     */
    public Map<String, Cookie> getCookies() {
        return this.httpServerExchange.getRequestCookies();
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
     */
    public String getPath() {
        return this.httpServerExchange.getRequestPath();
    }
}