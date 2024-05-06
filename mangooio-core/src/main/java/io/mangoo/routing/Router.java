package io.mangoo.routing;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.NotNull;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.routes.FileRoute;
import io.mangoo.routing.routes.PathRoute;
import io.mangoo.routing.routes.RequestRoute;
import io.mangoo.routing.routes.ServerSentEventRoute;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressFBWarnings(value = "PMB_POSSIBLE_MEMORY_BLOAT", justification = "Route size is limited")
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
        Objects.requireNonNull(route, NotNull.ROUTE);
        Preconditions.checkArgument(routes.size() <= MAX_ROUTES, "Maximum of " + MAX_ROUTES + " routes reached");
        
        routes.add(route);

        if (route instanceof RequestRoute requestRoute && requestRoute.getControllerClass() != null && StringUtils.isNotBlank(requestRoute.getControllerMethod())) {
            reverseRoutes.put((requestRoute.getControllerClass().getSimpleName().toLowerCase(Locale.ENGLISH) + ":" + requestRoute.getControllerMethod()).toLowerCase(Locale.ENGLISH), requestRoute);
        }
    }

    /**
     * @return An unmodifiable set of all configured routes
     */
    public static Set<MangooRoute> getRoutes() {
        return Collections.unmodifiableSet(routes);
    }
    
    /**
     * @return An unmodifiable set of all configured RequestRoutes
     */
    public static Stream<RequestRoute> getRequestRoutes() {
        return routes.stream()
                .filter(RequestRoute.class::isInstance)
                .map(RequestRoute.class::cast)
                .collect(Collectors.toUnmodifiableSet())
                .stream();
    }
    
    /**
     * @return An unmodifiable set of all configured FileRoutes
     */
    public static Stream<FileRoute> getFileRoutes() {
        return routes.stream()
                .filter(FileRoute.class::isInstance)
                .map(FileRoute.class::cast)
                .collect(Collectors.toUnmodifiableSet())
                .stream();
    }
    
    /**
     * @return An unmodifiable set of all configured PathRouts
     */
    public static Stream<PathRoute> getPathRoutes() {
        return routes.stream()
                .filter(PathRoute.class::isInstance)
                .map(PathRoute.class::cast)
                .collect(Collectors.toUnmodifiableSet())
                .stream();
    }
    
    /**
     * @return An unmodifiable set of all configured ServerSentEventRoutes
     */
    public static Stream<ServerSentEventRoute> getServerSentEventRoutes() {
        return routes.stream()
                .filter(ServerSentEventRoute.class::isInstance)
                .map(ServerSentEventRoute.class::cast)
                .collect(Collectors.toUnmodifiableSet())
                .stream();
    }
    
    /**
     * Retrieves a reverse route by its controller class and controller method
     * 
     * @param key The controller class and method in the form ControllerClass:ControllerMethod
     * @return A route object based on the given controller and method or null if none found
     */
    public static RequestRoute getReverseRoute(String key) {
        Objects.requireNonNull(key, NotNull.KEY);
        return reverseRoutes.get(key.toLowerCase(Locale.ENGLISH));
    }
    
    /**
     * Removes all routes from the router
     */
    public static void reset() {
        routes = ConcurrentHashMap.newKeySet();
        reverseRoutes = new ConcurrentHashMap<>();
    }
}