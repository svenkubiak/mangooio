package io.mangoo.routing.routes;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.enums.Http;
import io.mangoo.enums.Required;
import io.mangoo.interfaces.MangooRoute;

/**
 * 
 * @author svenkubiak
 *
 */
public class RequestRoute implements MangooRoute {
    private Class<?> controllerClass;
    private Http[] methods = {};
    private Http method;
    private String url;
    private String controllerMethod;
    private String username;
    private String password;
    private int limit;
    private boolean blocking;
    private boolean authentication;
    private boolean authorization;

    public RequestRoute(Http method) {
        Objects.requireNonNull(method, Required.HTTP_METHOD.toString());
        this.method = method;
    }
    
    public RequestRoute(Http... methods) {
        Objects.requireNonNull(methods, Required.HTTP_METHOD.toString());
        this.methods = Arrays.copyOf(methods, methods.length);
    }

    /**
     * Sets the URL for this route
     * 
     * @param url The URL for this route
     * 
     * @return RequestRoute instance
     */
    public RequestRoute to(String url) {
        Objects.requireNonNull(url, Required.URL.toString());
        
        if ('/' != url.charAt(0)) {
            url = "/" + url;
        }
        
        this.url = url;
        
        return this;
    }
    
    /**
     * Sets the controller method to response on request
     * 
     * @param method The controller method
     * @return RequestRoute instance
     */
    public RequestRoute respondeWith(String method) {
        Objects.requireNonNull(method, Required.CONTROLLER_METHOD.toString());
        this.controllerMethod = method;
        return this;
    }
    
    /**
     * Sets a request limit to the request
     * 
     * @param requestsPerSecond Number of requests per second
     * @return RequestRoute instance
     */
    public RequestRoute withRequestLimit(int requestsPerSecond) {
        this.limit = requestsPerSecond;
        return this;
    }
    
    /**
     * Sets the controller class of this request
     * 
     * @param clazz The controller class
     */
    public void withControllerClass(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CONTROLLER_CLASS.toString());
        this.controllerClass = clazz;
    }
    
    /**
     * Sets the HTTP method of this request
     * 
     * @param method The controller method
     */
    public void withHttpMethod(Http method) {
        Objects.requireNonNull(method, Required.METHOD.toString());
        this.method = method;
    }
    
    /**
     * Sets Basic HTTP authentication to all method on the defined controller class
     * 
     * @param username The username for basic authentication in cleartext
     * @param password The password for basic authentication in cleartext
     * 
     * @return RequestRoute instance
     */
    public RequestRoute withBasicAuthentication(String username, String password) {
        Objects.requireNonNull(username, Required.USERNAME.toString());
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        
        this.username = username;
        this.password = password;
        
        return this;
    }

    /**
     * Sets authentication to true for this route, default is false
     * 
     * @return RequestRoute instance
     */
    public RequestRoute withAuthentication() {
        this.authentication = true;
        return this;
    }
    
    /**
     * Sets authorization to true for this route, default is false
     * Also sets authentication to true, default is false
     * 
     * @return RequestRoute instance
     */
    public RequestRoute withAuthorization() {
        this.authentication = true;
        this.authorization = true;
        return this;
    }
    
    /**
     * Configures this request as long running request that is
     * executed in a different thread pool to not block the
     * non blocking I/O request
     * 
     * @return RequestRoute instance
     */
    public RequestRoute withNonBlocking() {
        this.blocking = true;
        return this;
    }
    
    @Override
    public String getUrl() {
        return url;
    }
    
    public boolean hasAuthentication() {
        return this.authentication;
    }

    public boolean hasAuthorization() {
        return this.authorization;
    }
    
    public boolean hasBasicAuthentication() {
        return StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasMultipleMethods() {
        return methods != null && methods.length > 0;
    }

    public Http[] getMethods() {
        return Arrays.copyOf(this.methods, this.methods.length);
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public String getControllerMethod() {
        return controllerMethod;
    }

    public int getLimit() {
        return limit;
    }

    public Http getMethod() {
        return method;
    }

    public boolean isBlocking() {
        return blocking;
    }
}