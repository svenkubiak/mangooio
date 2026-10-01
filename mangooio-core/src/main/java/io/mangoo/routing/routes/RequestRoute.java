package io.mangoo.routing.routes;

import io.mangoo.constants.Required;
import io.mangoo.enums.Http;
import io.mangoo.interfaces.MangooRoute;

import java.util.Arrays;
import java.util.Objects;

public class RequestRoute implements MangooRoute {
    private Class<?> controllerClass;
    private Http[] methods = {};
    private Http method;
    private String url;
    private String controllerMethod;
    private boolean blocking;
    private boolean authentication;

    public RequestRoute(Http method) {
        Objects.requireNonNull(method, Required.HTTP_METHOD);
        this.method = method;
    }
    
    public RequestRoute(Http... methods) {
        Objects.requireNonNull(methods, Required.HTTP_METHOD);
        this.methods = Arrays.copyOf(methods, methods.length);
    }

    public RequestRoute to(String url) {
        Objects.requireNonNull(url, Required.URL);
        
        if ('/' != url.charAt(0)) {
            url = "/" + url;
        }
        
        this.url = url;
        
        return this;
    }
    
    public RequestRoute respondeWith(String method) {
        Objects.requireNonNull(method, Required.CONTROLLER_METHOD);
        this.controllerMethod = method;
        return this;
    }
    
    RequestRoute forMethod(Http method) {
        Objects.requireNonNull(method, Required.HTTP_METHOD);

        var requestRoute = new RequestRoute(method);
        requestRoute.url = url;
        requestRoute.controllerClass = controllerClass;
        requestRoute.controllerMethod = controllerMethod;
        requestRoute.blocking = blocking;
        requestRoute.authentication = authentication;

        return requestRoute;
    }

    public void withControllerClass(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CONTROLLER_CLASS);
        this.controllerClass = clazz;
    }
    
    public void withHttpMethod(Http method) {
        Objects.requireNonNull(method, Required.METHOD);
        this.method = method;
    }

    public RequestRoute withAuthentication() {
        this.authentication = true;
        return this;
    }
    
    /** Executes the request in a worker thread pool, so that a long-running request does not block the I/O thread. */
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