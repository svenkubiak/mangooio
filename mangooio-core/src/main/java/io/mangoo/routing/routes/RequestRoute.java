package io.mangoo.routing.routes;

import io.mangoo.constants.NotNull;
import io.mangoo.enums.Http;
import io.mangoo.interfaces.MangooRoute;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public class RequestRoute implements MangooRoute {
    private Class<?> controllerClass;
    private Http[] methods = {};
    private Http method;
    private String url;
    private String controllerMethod;
    private String username;
    private String password;
    private boolean blocking;
    private boolean authentication;

    public RequestRoute(Http method) {
        Objects.requireNonNull(method, NotNull.HTTP_METHOD);
        this.method = method;
    }
    
    public RequestRoute(Http... methods) {
        Objects.requireNonNull(methods, NotNull.HTTP_METHOD);
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
        Objects.requireNonNull(url, NotNull.URL);
        
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
        Objects.requireNonNull(method, NotNull.CONTROLLER_METHOD);
        this.controllerMethod = method;
        return this;
    }
    
    /**
     * Sets the controller class of this request
     * 
     * @param clazz The controller class
     */
    public void withControllerClass(Class<?> clazz) {
        Objects.requireNonNull(clazz, NotNull.CONTROLLER_CLASS);
        this.controllerClass = clazz;
    }
    
    /**
     * Sets the HTTP method of this request
     * 
     * @param method The controller method
     */
    public void withHttpMethod(Http method) {
        Objects.requireNonNull(method, NotNull.METHOD);
        this.method = method;
    }
    
    /**
     * Sets HTTP Basic authentication to this request on the defined controller class
     * 
     * @param username The username for basic authentication in cleartext
     * @param password The password for basic authentication in cleartext
     * 
     * @return RequestRoute instance
     */
    public RequestRoute withBasicAuthentication(String username, String password) {
        Objects.requireNonNull(username, NotNull.USERNAME);
        Objects.requireNonNull(password, NotNull.PASSWORD);
        
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
     * Configures this request as long-running request that is
     * executed in a different thread pool to not block the
     * non-blocking I/O request
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
        return authentication;
    }
    
    public boolean hasBasicAuthentication() {
        return StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password);
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

    public Http getMethod() {
        return method;
    }

    public boolean isBlocking() {
        return blocking;
    }
}