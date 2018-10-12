package io.mangoo.routing;

import io.mangoo.enums.Http;
import io.mangoo.routing.routes.RequestRoute;

/**
 * 
 * @author svenkubiak
 *
 */
public class On {
    
    private On() {
    }

    /**
     * Creates a new RequestRoute for a HTTP GET request
     * 
     * @return RequestRoute instance
     */
    public static RequestRoute get() {
        return new RequestRoute(Http.GET);
    }
    
    /**
     * Creates a new RequestRoute for a HTTP POST request
     * 
     * @return RequestRoute instance
     */
    public static RequestRoute post() {
        return new RequestRoute(Http.POST);
    }
    
    /**
     * Creates a new RequestRoute for a HTTP PUT request
     * 
     * @return RequestRoute instance
     */
    public static RequestRoute put() {
        return new RequestRoute(Http.PUT);
    }
    
    /**
     * Creates a new RequestRoute for a HTTP PATCH request
     * 
     * @return RequestRoute instance
     */
    public static RequestRoute patch() {
        return new RequestRoute(Http.PATCH);
    }
    
    /**
     * Creates a new RequestRoute for a HTTP DELETE request
     * 
     * @return RequestRoute instance
     */
    public static RequestRoute delete() {
        return new RequestRoute(Http.DELETE);
    }
    
    /**
     * Creates a new RequestRoute for a HTTP OPTIONS request
     * 
     * @return RequestRoute instance
     */
    public static RequestRoute options() {
        return new RequestRoute(Http.OPTIONS);
    }
    
    /**
     * Creates a new RequestRoute for a HTTP HEAD request
     * 
     * @return RequestRoute instance
     */
    public static RequestRoute head() {
        return new RequestRoute(Http.HEAD);
    }
    
    /**
     * Creates a new RequestRoute for a given list of HTTP request methods
     * @param methods list of HTTP request methods
     * 
     * @return RequestRoute instance
     */
    public static RequestRoute anyOf(Http... methods) {
        return new RequestRoute(methods);
    }
}