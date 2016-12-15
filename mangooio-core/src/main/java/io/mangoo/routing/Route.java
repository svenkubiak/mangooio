package io.mangoo.routing;

import java.util.Objects;

import io.mangoo.enums.Required;
import io.mangoo.enums.RouteType;
import io.undertow.util.HttpString;

/**
 *
 * @author svenkubiak
 *
 */
public class Route {
    private final RouteType routeType;
    private Class<?> controllerClass;
    private String controllerMethod;
    private HttpString requestMethod;
    private String url;
    private String username;
    private String password;
    private int limit;
    private boolean authentication;
    private boolean blocking;
    private boolean timer;
    private boolean internalTemplateEngine;

    public Route(RouteType routeType) {
        this.routeType = Objects.requireNonNull(routeType, Required.ROUTE_TYPE.toString());
    }

    public Route toUrl(String url) {
        this.url = Objects.requireNonNull(url, Required.URL.toString());

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
    
    public Route withUsername(String username) {
        this.username = username;
        return this;
    }  
    
    public Route withPassword(String password) {
        this.password = password;
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

    public Route withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public String getUrl() {
        return this.url;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public String getUsername() {
        return this.username;
    }    

    public String getPassword() {
        return this.password;
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