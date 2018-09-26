package io.mangoo.routing;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;
import com.tc.text.StringUtils;

import io.mangoo.enums.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.routes.RequestRoute;

/**
 *
 * @author svenkubiak
 *
 */
public final class Router {
    private static Set<MangooRoute> routes = ConcurrentHashMap.newKeySet();
    private static Map<String, RequestRoute> reverseRoutes = new ConcurrentHashMap<>();
    private static final int MAX_ROUTES = 100000;

    private Router(){
    }

    /**
     * Adds a new route to the router
     *
     * @param route The route to add
     */
    public static void addRoute(MangooRoute route) {
        Objects.requireNonNull(route, Required.ROUTE.toString());
        Preconditions.checkArgument(routes.size() <= MAX_ROUTES, "Maximum of " + MAX_ROUTES + " routes reached");
        
        routes.add(route);

        if (route instanceof RequestRoute) {
            RequestRoute requestRoute = (RequestRoute) route;
            if (requestRoute.getControllerClass() != null && StringUtils.isNotBlank(requestRoute.getControllerMethod())) {
                reverseRoutes.put((requestRoute.getControllerClass().getSimpleName().toLowerCase(Locale.ENGLISH) + ":" + requestRoute.getControllerMethod()).toLowerCase(Locale.ENGLISH), requestRoute);    
            }   
        }
    }

    /**
     * @return An unmodifiable set of all configured routes
     */
    public static Set<MangooRoute> getRoutes() {
        return Collections.unmodifiableSet(routes);
    }
    
    /**
     * Retrieves a reverse route by its controller class and controller method
     * 
     * @param key The controller class and method in the form ControllerClass:ControllerMethod
     * @return A route object based on the given controller and method or null if none found
     */
    public static RequestRoute getReverseRoute(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        return (RequestRoute) reverseRoutes.get(key.toLowerCase(Locale.ENGLISH));
    }
    
    /**
     * Removes all routes from the router
     */
    public static void reset() {
        routes = ConcurrentHashMap.newKeySet();
        reverseRoutes = new ConcurrentHashMap<>();
    }
}