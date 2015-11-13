package io.mangoo.routing.routes;

import io.mangoo.enums.RouteType;
import io.mangoo.routing.Routing;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public class ControllerRoute extends Route {
    
    public ControllerRoute(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        this.routeType = RouteType.REQUEST;
    }

    public ControllerRoute get() {
        this.requestMethod = Methods.GET;
        
        return this;
    }
    
    public ControllerRoute post() {
        this.requestMethod = Methods.POST;
        
        return this;
    }
    
    public ControllerRoute put() {
        this.requestMethod = Methods.PUT;
        
        return this;
    }
    
    public ControllerRoute delete() {
        this.requestMethod = Methods.DELETE;
        
        return this;
    }
    
    public ControllerRoute to(String url) {
        this.url = url;
        
        return this;
    }
    
    public ControllerRoute call(String controllerMethod) {
        this.controllerMethod = controllerMethod;
        
        return this;
    }
    
    public ControllerRoute add() {
        Routing.addRoute(this);
        
        return new ControllerRoute(this.controllerClass);
    }
    
    public String getControllerMethod() {
        return this.controllerMethod;
    }
    
    public HttpString getRequestMethod() {
        return this.requestMethod;
    }
}