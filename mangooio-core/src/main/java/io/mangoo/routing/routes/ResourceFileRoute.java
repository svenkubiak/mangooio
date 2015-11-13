package io.mangoo.routing.routes;

import io.mangoo.enums.RouteType;
import io.mangoo.routing.Routing;

/**
 * 
 * @author svenkubiak
 *
 */
public class ResourceFileRoute extends Route {
    
    public ResourceFileRoute() {
        this.routeType = RouteType.RESOURCE_FILE;
    }
    
    public ResourceFileRoute to(String url) {
        this.url = url;
        
        return this;
    }
    
    public ResourceFileRoute add() {
        Routing.addRoute(this);
        
        return new ResourceFileRoute();
    }
}