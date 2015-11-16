package io.mangoo.routing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

/**
 * 
 * @author svenkubiak
 *
 */
public final class Router {
    private static Set<Route> routes = new HashSet<>();
    
    private Router(){
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
    
    /**
     * @return An unmodifiable set of all configured routes
     */
    public static Set<Route> getRoutes() {
        return Collections.unmodifiableSet(routes);
    }
}