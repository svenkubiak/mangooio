package io.mangoo.routing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

import io.mangoo.routing.routes.ControllerRoute;
import io.mangoo.routing.routes.ResourceFileRoute;
import io.mangoo.routing.routes.ResourcePathRoute;
import io.mangoo.routing.routes.Route;
import io.mangoo.routing.routes.ServerSentEventRoute;
import io.mangoo.routing.routes.WebSocketRoute;

/**
 * 
 * @author svenkubiak
 *
 */
public final class Routing {
    private static Set<Route> routes = new HashSet<>();
    
    public static ControllerRoute ofController(Class<?> clazz) {
        return new ControllerRoute(clazz);
    }
    
    public static WebSocketRoute ofWebSocket(Class<?> clazz) {
        return new WebSocketRoute(clazz);
    }
    
    public static ServerSentEventRoute ofServerSentEvent() {
        return new ServerSentEventRoute();
    }
    
    public static ResourceFileRoute ofResourceFile() {
        return new ResourceFileRoute();
    }
    
    public static ResourcePathRoute ofResourcePath() {
        return new ResourcePathRoute();
    }

    /**
     * Adds a new route to the router
     * 
     * @param route The route to add
     */
    public static void addRoute(Route route) {
        Preconditions.checkNotNull(route, "route can note be null");

        routes.add(route);
    }
    
    public static Set<Route> getRoutes() {
        return Collections.unmodifiableSet(routes);
    }
}