package io.mangoo.routing.routes;

import io.mangoo.enums.RouteType;
import io.mangoo.routing.Routing;

/**
 * 
 * @author svenkubiak
 *
 */
public class ServerSentEventRoute extends Route {

    public ServerSentEventRoute() {
        this.routeType = RouteType.SERVER_SENT_EVENT;
    }
    
    public ServerSentEventRoute to(String url) {
        this.url = url;
        
        return this;
    }
    
    public ServerSentEventRoute withAuthentication() {
        this.authentication = true;
        
        return this;
    }
    
    public ServerSentEventRoute add() {
        Routing.addRoute(this);
        
        return new ServerSentEventRoute();
    }
    
    public boolean isAuthentication() {
        return this.authentication;
    }
}