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
    private boolean timer;
    private boolean internalTemplateEngine;

    public Route(RouteType routeType) {
        this.routeType = Objects.requireNonNull(routeType, "routeType can not be null");
    }

    public Route toUrl(String url) {
        this.url = Objects.requireNonNull(url, "url can not be null");

        if (RouteType.RESOURCE_PATH == this.routeType) {
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

    public Route withTimer(boolean timer) {
        this.timer = timer;
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
    
    public Route useInternalTemplateEngine() {
        this.internalTemplateEngine = true;
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

    public HttpString getRequestMethod() {
        return this.requestMethod;
    }

    public boolean isAuthenticationRequired() {
        return this.authentication;
    }
    
    public boolean isInternalTemplateEngine() {
        return this.internalTemplateEngine;
    }

    public boolean isBlockingAllowed() {
        return this.blocking;
    }
    
    public boolean isTimerEnabled() {
        return this.timer;
    }
}