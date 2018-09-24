package io.mangoo.routing.routes;

import java.util.Objects;

import io.mangoo.enums.Http;
import io.mangoo.enums.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Response;

/**
 * 
 * @author svenkubiak
 *
 */
public class RequestRoute implements MangooRoute {
    private Http[] methods;
    private String url;
    private Class<?> controllerClass;
    private String controllerMethod;
    private int limit;
    private Http method;
    private boolean blocking;
    private Response response;

    public RequestRoute(Http method) {
        Objects.requireNonNull(method, Required.HTTP_METHOD.toString());
        
        this.method = method;
    }
    
    public RequestRoute(Http... methods) {
        Objects.requireNonNull(method, Required.HTTP_METHOD.toString());
        
        this.methods = methods;
    }

    public RequestRoute to(String url) {
        Objects.requireNonNull(url, Required.URL.toString());
        this.url = url;
        
        return this;
    }
    
    public RequestRoute respondeWith(Response response) {
        Objects.requireNonNull(response, Required.RESPONSE.toString());
        
        this.response = response;
        
        return this;
    }
    
    public RequestRoute respondeWith(String method) {
        Objects.requireNonNull(method, Required.CONTROLLER_METHOD.toString());
        
        this.controllerMethod = method;
        
        return this;
    }
    
    public RequestRoute maxRequestsPerSecond(int requestsPerSecond) {
        this.limit = requestsPerSecond;
        
        return this;
    }
    
    public RequestRoute canBlock() {
        this.blocking = true;
        
        return this;
    }
    
    public void withControllerClass(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CONTROLLER_CLASS.toString());
        
        this.controllerClass = clazz;
    }
    
    public void withHttpMethod(Http method) {
        Objects.requireNonNull(method, Required.METHOD.toString());
        
        this.method = method;
    }
    
    public boolean hasMultipleMethods() {
        return methods != null && methods.length > 0;
    }

    public Http[] getMethods() {
        return methods;
    }

    public String getUrl() {
        return url;
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

    public Response getResponse() {
        return response;
    }
}