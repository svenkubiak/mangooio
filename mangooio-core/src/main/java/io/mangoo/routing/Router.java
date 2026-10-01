package io.mangoo.routing;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.routes.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressFBWarnings(value = "PMB_POSSIBLE_MEMORY_BLOAT", justification = "Route size is limited")
public final class Router {
    private static final Logger LOG = LogManager.getLogger(Router.class);
    private static final int MAX_ROUTES = 100000;
    private static final List<String> urls = new ArrayList<>();
    private static Set<MangooRoute> routes = ConcurrentHashMap.newKeySet();
    private static Map<String, RequestRoute> reverseRoutes = new ConcurrentHashMap<>();

    private Router(){
    }

    public static void addRoute(MangooRoute route, String type) {
        Objects.requireNonNull(route, Required.ROUTE);
        Objects.requireNonNull(type, Required.TYPE);
        Preconditions.checkArgument(routes.size() <= MAX_ROUTES, "Maximum of " + MAX_ROUTES + " routes reached");

        urls.add(type.toUpperCase() + " " + route.getUrl());
        routes.add(route);

        if (route instanceof RequestRoute requestRoute && requestRoute.getControllerClass() != null && StringUtils.isNotBlank(requestRoute.getControllerMethod())) {
            reverseRoutes.put((requestRoute.getControllerClass().getSimpleName().toLowerCase(Locale.ENGLISH) + ":" + requestRoute.getControllerMethod()).toLowerCase(Locale.ENGLISH), requestRoute);
        }
    }

    public static boolean validUrls() {
        HashSet<String> uniqueUrls = new HashSet<>();
        List<String> duplicates = urls.stream()
                .filter(url -> !uniqueUrls.add(url))
                .toList();

        if (!duplicates.isEmpty()) {
            duplicates.forEach(duplicate -> LOG.error("Found multiple mappings of URL mapping '{}'", duplicate));
            return false;
        }

        return true;
    }

    public static Set<MangooRoute> getRoutes() {
        return Collections.unmodifiableSet(routes);
    }
    
    public static Stream<RequestRoute> getRequestRoutes() {
        return routes.stream()
                .filter(RequestRoute.class::isInstance)
                .map(RequestRoute.class::cast)
                .collect(Collectors.toUnmodifiableSet())
                .stream();
    }
    
    public static Stream<FileRoute> getFileRoutes() {
        return routes.stream()
                .filter(FileRoute.class::isInstance)
                .map(FileRoute.class::cast)
                .collect(Collectors.toUnmodifiableSet())
                .stream();
    }
    
    public static Stream<PathRoute> getPathRoutes() {
        return routes.stream()
                .filter(PathRoute.class::isInstance)
                .map(PathRoute.class::cast)
                .collect(Collectors.toUnmodifiableSet())
                .stream();
    }
    
    public static Stream<ServerSentEventRoute> getServerSentEventRoutes() {
        return routes.stream()
                .filter(ServerSentEventRoute.class::isInstance)
                .map(ServerSentEventRoute.class::cast)
                .collect(Collectors.toUnmodifiableSet())
                .stream();
    }

    public static Stream<WebSocketRoute> getWebSocketRoutes() {
        return routes.stream()
                .filter(WebSocketRoute.class::isInstance)
                .map(WebSocketRoute.class::cast)
                .collect(Collectors.toUnmodifiableSet())
                .stream();
    }
    
    // Key format is ControllerClass:controllerMethod (case-insensitive); returns null if no route is found.
    public static RequestRoute getReverseRoute(String key) {
        Objects.requireNonNull(key, Required.KEY);
        return reverseRoutes.get(key.toLowerCase(Locale.ENGLISH));
    }
    
    public static void reset() {
        routes = ConcurrentHashMap.newKeySet();
        reverseRoutes = new ConcurrentHashMap<>();
    }
}