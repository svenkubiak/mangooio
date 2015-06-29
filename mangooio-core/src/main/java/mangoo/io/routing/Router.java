package mangoo.io.routing;

import java.util.ArrayList;
import java.util.List;

import io.undertow.util.HttpString;
import mangoo.io.enums.RouteType;

/**
 *
 * @author svenkubiak
 *
 */
public final class Router {
    private static List<Route> routes = new ArrayList<Route>();

    private Router() {
    }

    public static void addRoute(Route route) {
        routes.add(route);
    }

    /**
     * Creates a request mapping using the given request method
     *
     * @param requestMethod The request method (e.g. Methods.GET)
     * @return A route object {@link mangoo.io.routing.Route}
     */
    public static Route mapRequest(HttpString requestMethod) {
        return new Route(requestMethod);
    }

    /**
     * Creates a request mapping for a websocket
     * @return A route object {@link mangoo.io.routing.Route}
     */
    public static Route mapWebSocket() {
        return new Route(RouteType.WEBSOCKET);
    }

    /**
     * Creates a request mapping for a resource file, e.g. /robots.txt
     * @return A route object {@link mangoo.io.routing.Route}
     */
    public static Route mapResourceFile() {
        return new Route(RouteType.RESOURCE_FILE);
    }

    /**
     * Creates a request mapping for resource, e.g. /assets/javascripts
     * @return A route object {@link mangoo.io.routing.Route}
     */
    public static Route mapResourcePath() {
        return new Route(RouteType.RESOURCE_PATH);
    }

    public static List<Route> getRoutes() {
        return routes;
    }
}