package mangoo.io.routing;

import io.undertow.util.HttpString;

import java.util.ArrayList;
import java.util.List;

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

    public static Route mapRequest(HttpString requestMethod) {
        return new Route(requestMethod);
    }

    public static Route mapWebSocket() {
        return new Route(RouteType.WEBSOCKET);
    }

    public static Route mapResourceFile() {
        return new Route(RouteType.RESOURCE_FILE);
    }

    public static Route mapResourcePath() {
        return new Route(RouteType.RESOURCE_PATH);
    }

    public static List<Route> getRoutes() {
        return routes;
    }
}