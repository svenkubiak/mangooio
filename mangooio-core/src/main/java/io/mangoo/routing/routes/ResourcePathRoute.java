package io.mangoo.routing.routes;

import io.mangoo.enums.RouteType;
import io.mangoo.routing.Routing;

/**
 * 
 * @author svenkubiak
 *
 */
public class ResourcePathRoute extends Route {
    
    public ResourcePathRoute() {
        this.routeType = RouteType.RESOURCE_PATH;
    }
    
    public ResourcePathRoute to(String url) {
        this.url = url;
        
        return this;
    }
    
    public ResourcePathRoute add() {
        Routing.addRoute(this);
        
        return new ResourcePathRoute();
    }
}
