package io.mangoo.routing.routes;

import java.util.Objects;

import io.mangoo.enums.Http;
import io.mangoo.enums.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;

/**
 * 
 * @author svenkubiak
 *
 */

public class ControllerRoute {
    private Class<?> controllerClass;
    
    public ControllerRoute(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CONTROLLER_CLASS.toString());
        
        this.controllerClass = clazz;
    }

    public void with(MangooRoute... routes) {
        Objects.requireNonNull(routes, Required.ROUTE.toString());

        for (MangooRoute route : routes) {
            RequestRoute requestRoute = (RequestRoute) route;
            requestRoute.withControllerClass(this.controllerClass);
            
            if (requestRoute.hasMultipleMethods()) {
                for (Http method : requestRoute.getMethods()) {
                    requestRoute.withHttpMethod(method);
                    Router.addRoute(requestRoute);
                }
            } else {
                Router.addRoute(requestRoute);
            }
        }
    }
}