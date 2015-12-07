package io.mangoo.routing;

import java.util.Objects;

import io.mangoo.enums.RouteType;
import io.undertow.util.HttpString;

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
    private final RouteType routeType;
    private boolean authentication;
    private boolean blocking;

    public Route(RouteType routeType) {
        this.routeType = Objects.requireNonNull(routeType, "routeType can not be null");
    }

    public Route toUrl(String url) {
        this.url = url;

        if (RouteType.RESOURCE_PATH.equals(this.routeType)) {
            if ('/' != this.url.charAt(0)) {
                this.url = "/" + this.url;
            }

            if (!this.url.endsWith("/")) {
                this.url = this.url + "/";
            }
        } else {
            if ('/' != this.url.charAt(0)) {
                this.url = "/" + this.url;
            }
        }

        return this;
    }

    public Route withClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        return this;
    }

    public Route withMethod(String controllerMethod) {
        this.controllerMethod = controllerMethod;
        return this;
    }

    public Route withRequest(HttpString requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    public Route withAuthentication(boolean authentication) {
        this.authentication = authentication;
        return this;
    }

    public Route allowBlocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }

    public String getUrl() {
        return this.url;
    }

    public RouteType getRouteType() {
        return this.routeType;
    }

    public Class<?> getControllerClass() {
        return this.controllerClass;
    }

    public String getControllerMethod() {
        return this.controllerMethod;
    }

    public boolean isAuthenticationRequired() {
        return this.authentication;
    }

    public HttpString getRequestMethod() {
        return this.requestMethod;
    }

    public boolean isBlockingAllowed() {
        return this.blocking;
    }
}