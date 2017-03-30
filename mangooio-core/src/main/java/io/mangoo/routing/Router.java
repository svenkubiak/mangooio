package io.mangoo.routing;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;

import io.mangoo.enums.Required;
import io.mangoo.enums.RouteType;

/**
 *
 * @author svenkubiak
 *
 */
public final class Router {
    private static Set<Route> routes = ConcurrentHashMap.newKeySet();
    private static Map<String, Route> reverseRoutes = new ConcurrentHashMap<>();
    private static final int MAX_ROUTES = 100000;

    private Router(){
    }

    /**
     * Adds a new route to the router
     *
     * @param route The route to add
     */
    public static void addRoute(Route route) {
        Objects.requireNonNull(route, Required.ROUTE.toString());
        Preconditions.checkArgument(routes.size() <= MAX_ROUTES, "Maximum of " + MAX_ROUTES + " routes reached");
        
        routes.add(route);
        if (route.getRouteType() == RouteType.REQUEST) {
            reverseRoutes.put(route.getControllerClass().getSimpleName() + ":" + route.getControllerMethod(), route);    
        }
    }

    /**
     * @return An unmodifiable set of all configured routes
     */
    public static Set<Route> getRoutes() {
        return Collections.unmodifiableSet(routes);
    }
    
    /**
     * Retrieves a reverse route by its key
     * 
     * @param key The passed route information (e.g. /foo/{bar})
     * @return A route object based on the given controller and method or null if none found
     */
    public static Route getReverseRoute(String key) {
        return reverseRoutes.get(key);
    }
}