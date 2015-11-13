package io.mangoo.routing.routes;

import io.mangoo.enums.RouteType;
import io.mangoo.routing.Routing;

/**
 * 
 * @author svenkubiak
 *
 */
public class WebSocketRoute extends Route {

    public WebSocketRoute(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        this.routeType = RouteType.WEBSOCKET;
    }
    
    public WebSocketRoute to(String url) {
        this.url = url;
        
        return this;
    }
    
    public WebSocketRoute withAuthentication() {
        this.authentication = true;
        
        return this;
    }
    
    public WebSocketRoute add() {
        Routing.addRoute(this);
        
        return new WebSocketRoute(this.controllerClass);
    }
    
    public boolean isAuthentication() {
        return this.authentication;
    }
}