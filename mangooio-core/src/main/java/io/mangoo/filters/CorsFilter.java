package io.mangoo.filters;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.filters.cors.CorsPolicy;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;

public class CorsFilter implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(CorsFilter.class);
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    private static final String SIMPLE_RESPONSE_HEADERS = "Cache-Control,Content-Language,Content-Type,Expires,Last-Modified,Pragma";
    private static final String DEFAULT_URL_PATTERN = "^.*$";
    private static final String DEFAULT_POLICY_CLASS = "com.stijndewitt.undertow.cors.AllowAll";
    private static final String DEFAULT_POLICY_PARAM = "";
    private static final String DEFAULT_MAX_AGE = "864000"; // 10 days
    private static final String DEFAULT_ALLOW_CREDENTIALS = "true";
    private static final String DEFAULT_ALLOW_METHODS = "DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT";
    private static final String DEFAULT_ALLOW_HEADERS = "Authorization,Content-Type,Link,X-Total-Count,Range";
    private static final String DEFAULT_EXPOSE_HEADERS = "Accept-Ranges,Content-Length,Content-Range,ETag,Link,Server,X-Total-Count";

    private HttpHandler next;
    private String urlPattern;
    private String policyClass;
    private String policyParam;
    private String exposeHeaders;
    private String maxAge;
    private String allowCredentials;
    private String allowMethods;
    private String allowHeaders;
    
    private transient CorsPolicy policy;
    private transient Pattern pattern;

    public CorsFilter(HttpHandler next) {
        super();
        this.next = next;
    }

    public void setUrlPattern(String pattern) {
        urlPattern = pattern;
        LOG.info("undertow-cors-filter: urlPattern=" + getUrlPattern());
    }

    public String getUrlPattern() {
        return urlPattern != null ? urlPattern : DEFAULT_URL_PATTERN;
    }

    public void setPolicyClass(String name) {
        policy = null;
        policyClass = name;
        LOG.info("undertow-cors-filter: policyClass=" + getPolicyClass());
    }

    public String getPolicyClass() {
        return policyClass != null ? policyClass : DEFAULT_POLICY_CLASS;
    }

    public void setPolicyParam(String value) {
        policy = null;
        policyParam = value;
        LOG.info("undertow-cors-filter: policyParam=" + getPolicyParam());
    }

    public String getPolicyParam() {
        return policyParam != null ? policyParam : DEFAULT_POLICY_PARAM;
    }

    public void setExposeHeaders(String value) {
        exposeHeaders = value;
        LOG.info("undertow-cors-filter: exposeHeaders=" + getExposeHeaders());
    }

    public String getExposeHeaders() {
        return exposeHeaders != null ? exposeHeaders : DEFAULT_EXPOSE_HEADERS;
    }

    public void setMaxAge(String value) {
        maxAge = value;
        LOG.info("undertow-cors-filter: maxAge=" + getMaxAge());
    }

    public String getMaxAge() {
        return maxAge != null ? maxAge : DEFAULT_MAX_AGE;
    }

    public void setAllowCredentials(String value) {
        allowCredentials = value;
        LOG.info("undertow-cors-filter: allowCredentials=" + getAllowCredentials());
    }

    public String getAllowCredentials() {
        return allowCredentials != null ? allowCredentials : DEFAULT_ALLOW_CREDENTIALS;
    }

    public void setAllowMethods(String value) {
        allowMethods = value;
        LOG.info("undertow-cors-filter: allowMethods=" + getAllowMethods());
    }

    public String getAllowMethods() {
        return allowMethods != null ? allowMethods : DEFAULT_ALLOW_METHODS;
    }

    public void setAllowHeaders(String value) {
        allowHeaders = value;
        LOG.info("undertow-cors-filter: allowHeaders=" + getAllowHeaders());
    }

    public String getAllowHeaders() {
        return allowHeaders != null ? allowHeaders : DEFAULT_ALLOW_HEADERS;
    }

    /**
     * Creates the policy from the policy class with the given {@code name}, passing the given {@code param} to the constructor.
     *  
     * @param name The name of the policy class, never {@code null}.
     * @param param The parameter to pass to the policy, possibly {@code null}.
     * @return The created policy, or {@code null} if the policy class could not be found or instantiation failed.
     */
    public CorsPolicy createPolicy(String name, String param) {
        Class<? extends CorsPolicy> P = null;
        CorsPolicy result = null;
        try {P = Class.forName(name).asSubclass(CorsPolicy.class);} 
        catch (ClassNotFoundException e) {
            //LOG.log(Level.SEVERE, "undertow-cors-filter: Policy class " + name + " not found.", e);
            return null;
        }
        try {result = P.getConstructor(String.class).newInstance(policyParam);} 
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            //LOG.log(Level.SEVERE, "undertow-cors-filter: Unable to instantiate policy class " + name + " with parameter \"" + policyParam + "\".", e);
            return null;
        }
            LOG.info("undertow-cors-filter: Created policy from policy class " + name + " with param \"" + param + "\".");
        return result;
    }

    /**
     * Handles the incoming request.
     * 
     * <p>This method tests whether the request given in {@code exchange} should be filtered, based
     * on the request URL and the configured {@code urlPattern}, and if so, calls {@code applyPolicy}
     * to apply the policy configured in {@code policyClass} and {@code plocyParam}.</p>
     * 
     * @param exchange The server exchange we got from Undertow, never {@code null}.
     * 
     * @see #applyPolicy
     */
    @Override public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            // This code is executed by one of the XNIO I/O threads.
            // It is very important NOT to run anything that could block the thread. 
            exchange.dispatch(this);
            return;
        }
        
        // This code is executed by a worker thread. It's save to do blocking I/O here.
        String url = url(exchange);
        if (pattern == null) pattern = Pattern.compile(getUrlPattern());
        if (pattern.matcher(url).matches()) {
                LOG.info("undertow-cors-filter: handling request " + url);
            String origin = origin(exchange);
            boolean allowed = applyPolicy(exchange, origin);
                LOG.info("undertow-cors-filter: CORS headers " + (allowed ? "" : "NOT ") + "added for origin " + origin);
        } else {
                LOG.info("undertow-cors-filter: NOT handling request " + url + ". Does not match urlPattern \"" + urlPattern + "\".");
        }
        next.handleRequest(exchange);
    }

    /**
     * Applies the policy configured in {@code policyClass} and {@code policyParam} and if the 
     * given {@code origin} is allowed access, adds the response headers as configured.
     *  
     * @param exchange The server exchange we got from Undertow, never {@code null}.
     * @param origin The origin that requests access, may be {@code null}.
     * @return {@code true} if {@code origin} was allowed access, {@code false} otherwise.
     */
    public boolean applyPolicy(HttpServerExchange exchange, String origin) {
        if (policy == null) policy = createPolicy(getPolicyClass(), getPolicyParam());
        if (policy != null && origin != null && policy.isAllowed(origin)) {
            if (!hasHeader(exchange, ACCESS_CONTROL_ALLOW_ORIGIN))      addHeader(exchange, ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            if (!hasHeader(exchange, ACCESS_CONTROL_ALLOW_HEADERS))     addHeader(exchange, ACCESS_CONTROL_ALLOW_HEADERS, getAllowHeaders());
            if (!hasHeader(exchange, ACCESS_CONTROL_ALLOW_CREDENTIALS)) addHeader(exchange, ACCESS_CONTROL_ALLOW_CREDENTIALS, getAllowCredentials());
            if (!hasHeader(exchange, ACCESS_CONTROL_ALLOW_METHODS))     addHeader(exchange, ACCESS_CONTROL_ALLOW_METHODS, getAllowMethods());
            if (!hasHeader(exchange, ACCESS_CONTROL_EXPOSE_HEADERS))    addHeader(exchange, ACCESS_CONTROL_EXPOSE_HEADERS, getExposeHeaders());
            if (!hasHeader(exchange, ACCESS_CONTROL_MAX_AGE))           addHeader(exchange, ACCESS_CONTROL_MAX_AGE, getMaxAge());
            return true;
        }
        return false;
    }
    
    /**
     * Gets the Origin header.
     * 
     * @param exchange The server exchange we got from Undertow, never {@code null}.
     * 
     * @return The Origin header string, may be {@code null}.
     */
    protected String origin(HttpServerExchange exchange) {
        HeaderValues headers = ((HttpServerExchange) exchange).getRequestHeaders().get("Origin");
        return headers == null ? null : headers.peekFirst();
    }

    /**
     * Gets the request URL including querystring.
     * 
     * @param exchange The server exchange we got from Undertow, never {@code null}.
     * 
     * @return The request URL, never {@code null}.
     */
    protected String url(HttpServerExchange exchange) {
        return exchange.getRequestURL() + (exchange.getQueryString() == null || exchange.getQueryString().isEmpty() ? "" : "?" + exchange.getQueryString());  
    }
    
    /**
     * Checks whether the header with {@code name} is already present on the response.
     * 
     * @param exchange The server exchange we got from Undertow, never {@code null}.
     * @param name The name of the header to check, never {@code null}.
     * @return {@code true} if the header is already present, {@code false} otherwise.
     */
    protected boolean hasHeader(HttpServerExchange exchange, String name) {
        return exchange.getResponseHeaders().get(name) != null;
    }
    
    /**
     * Adds the response header with {@code name} and {@code value}.
     * @param exchange The server exchange we got from Undertow, never {@code null}.
     * @param name The name of the header to add, never {@code null}.
     * @param value The value of the header to add, never {@code null}.
     */
    protected void addHeader(HttpServerExchange exchange, String name, String value) {
        exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
    }
}