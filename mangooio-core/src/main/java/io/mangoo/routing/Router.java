package io.mangoo.routing;

import java.util.Collections;
import java.util.Locale;
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
            reverseRoutes.put((route.getControllerClass().getSimpleName() + ":" + route.getControllerMethod()).toLowerCase(Locale.ENGLISH), route);    
        }
    }

    /**
     * @return An unmodifiable set of all configured routes
     */
    public static Set<Route> getRoutes() {
        return Collections.unmodifiableSet(routes);
    }
    
    /**
     * Retrieves a reverse route by its controller class and controller method
     * 
     * @param key The controller class and method in the form ControllerClass:ControllerMethod
     * @return A route object based on the given controller and method or null if none found
     */
    public static Route getReverseRoute(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        return reverseRoutes.get(key.toLowerCase(Locale.ENGLISH));
    }
}