package io.mangoo.routing;

import io.mangoo.enums.Http;
import io.mangoo.routing.routes.RequestRoute;

/**
 * 
 * @author svenkubiak
 *
 */
public class On {

    public static RequestRoute get() {
        return new RequestRoute(Http.GET);
    }
    
    public static RequestRoute post() {
        return new RequestRoute(Http.POST);
    }
    
    public static RequestRoute put() {
        return new RequestRoute(Http.PUT);
    }
    
    public static RequestRoute patch() {
        return new RequestRoute(Http.PATCH);
    }
    
    public static RequestRoute delete() {
        return new RequestRoute(Http.DELETE);
    }
    
    public static RequestRoute options() {
        return new RequestRoute(Http.OPTIONS);
    }
    
    public static RequestRoute head() {
        return new RequestRoute(Http.HEAD);
    }
    
    public static RequestRoute anyOf(Http... methods) {
        return new RequestRoute(methods);
    }
}