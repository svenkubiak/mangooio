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

    public void onClassAndMethod(Class<?> controllerClass, String controllerMethod) {
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;

        Router.addRoute(this);
    }

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