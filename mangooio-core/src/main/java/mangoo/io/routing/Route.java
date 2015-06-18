package mangoo.io.routing;

import io.undertow.util.HttpString;
import mangoo.io.enums.RouteType;

/**
 *
 * @author svenkubiak
 *
 */
public class Route {
    private Class<?> controllerClass;
    private String controllerMethod;
    private HttpString requestMethod;
    private String url;
    private RouteType routeType;

    public Route(HttpString requestMethod) {
        this.routeType = RouteType.REQUEST;
        this.requestMethod = requestMethod;
    }

    public Route(RouteType routeType) {
        this.routeType = routeType;
    }

    /**
     * Maps a request mapping to given URL
     *
     * @param url The URL of the request (e.g. /foo)
     */
    public Route toUrl(String url) {
        this.url = url;

        if (RouteType.RESOURCE_PATH.equals(this.routeType)) {
            if (!this.url.startsWith("/")) {
                this.url = "/" + this.url;
            }

            if (!this.url.endsWith("/")) {
                this.url = this.url + "/";
            }
        } else {
            if (!this.url.startsWith("/")) {
                this.url = "/" + this.url;
            }
        }

        if (isResource()) {
            Router.addRoute(this);
        }

        return this;
    }

    private boolean isResource() {
        return RouteType.RESOURCE_FILE.equals(this.routeType) || RouteType.RESOURCE_PATH.equals(this.routeType);
    }

    /**
     * Maps the request to a given controller class and controller method
     *
     * @param controllerClass The controller class (e.g. ApplicationController)
     * @param controllerMethod The controller method (e.g. index)
     */
    public void onClassAndMethod(Class<?> controllerClass, String controllerMethod) {
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;

        Router.addRoute(this);
    }

    /**
     * Maps the request to a given controller class. Used for websockets as they have specific controller
     * methods.
     *
     * @param controllerClass The controller class (e.g. ApplicationController)
     */
    public void onClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;

        Router.addRoute(this);
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public String getControllerMethod() {
        return controllerMethod;
    }

    public HttpString getRequestMethod() {
        return requestMethod;
    }

    public String getUrl() {
        return url;
    }

    public RouteType getRouteType() {
        return routeType;
    }
}