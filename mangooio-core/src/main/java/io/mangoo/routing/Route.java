package io.mangoo.routing;

import com.google.common.base.Preconditions;

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
    private boolean blocking;
    private boolean authentication;

    public Route(HttpString requestMethod) {
        Preconditions.checkNotNull(requestMethod, "requestMethod can not be null");

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
     * @return A route object {@link io.mangoo.routing.Route}
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

        if (isResourceOrServerSentEvent()) {
            Router.addRoute(this);
        }

        return this;
    }

    private boolean isResourceOrServerSentEvent() {
        return RouteType.RESOURCE_FILE.equals(this.routeType)
                || RouteType.RESOURCE_PATH.equals(this.routeType)
                || RouteType.SERVER_SENT_EVENT.equals(this.routeType);
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

    /**
     * Sets a token with is used for signed authentication
     * of a Server Sent Event Connection
     *
     * @param token The token
     */
    
    /**
     * Sets a flag to the request that it requires
     * a valid authentication cookie
     * 
     * Please note, that this is only used for WebSockets
     * and ServerSentEvents
     * 
     * @return A route object {@link io.mangoo.routing.Route}
     */
    public Route withAuthentication() {
        this.authentication = true;
        
        return this;
    }

    /**
     * Sets a flag to the route, that blocking is possible.
     * This advises the response handler to pass the request to
     * a thread where blocking is allowed.
     * 
     * Please note, that this is only used in mapped controllers.
     */
    public void allowBlocking() {
        this.blocking = true;
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

    public boolean isAuthenticationRequired() {
        return this.authentication;
    }

    public boolean isBlocking() {
        return this.blocking;
    }
}