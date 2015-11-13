package io.mangoo.routing.routes;

import io.mangoo.enums.RouteType;
import io.undertow.util.HttpString;

/**
 *
 * @author svenkubiak
 *
 */
public class Route {
    protected Class<?> controllerClass;
    protected String controllerMethod;
    protected HttpString requestMethod;
    protected String url;
    protected RouteType routeType;
    protected boolean authentication;
    protected boolean blocking;

    public String getUrl() {
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