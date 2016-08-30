package io.mangoo.routing;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;

/**
 *
 * @author svenkubiak
 *
 */
public final class Router {
    private static Set<Route> routes = ConcurrentHashMap.newKeySet();
    private static int MAX_ROUTES = 100000;

    private Router(){
    }

    /**
     * Adds a new route to the router
     *
     * @param route The route to add
     */
    public static void addRoute(Route route) {
        Objects.requireNonNull(route, "route can note be null");
        Preconditions.checkArgument(routes.size() <= MAX_ROUTES, "Maximum of " + MAX_ROUTES + " routes reached");
        
        routes.add(route);
    }

    /**
     * @return An unmodifiable set of all configured routes
     */
    public static Set<Route> getRoutes() {
        return Collections.unmodifiableSet(routes);
    }
}