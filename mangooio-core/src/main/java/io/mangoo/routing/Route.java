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

    /**
     * Maps the request to a given controller class and controller method
     *
     * @param controllerClass The controller class (e.g. ApplicationController)
     * @param controllerMethod The controller method (e.g. index)
     * 
     * @return A route object {@link io.mangoo.routing.Route}
     */
    public Route onController(Class<?> controllerClass, String controllerMethod) {
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
        
        return this;
    }

    /**
     * Maps the request to a given controller class. Used for websockets as they have specific controller
     * methods.
     *
     * @param controllerClass The controller class (e.g. ApplicationController)
     * 
     * @return A route object {@link io.mangoo.routing.Route}
     */
    public Route onController(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        
        return this;
    }
    
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
     * 
     * @return A route object {@link io.mangoo.routing.Route}
     */
    public Route allowBlocking() {
        this.blocking = true;
        
        return this;
    }
    
    /**
     * Finished the route building process by adding this route to the router
     */
    public void build() {
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

    public boolean isAuthenticationRequired() {
        return this.authentication;
    }

    public boolean isBlocking() {
        return this.blocking;
    }
}