package io.mangoo.routing.routes;

import io.mangoo.constants.Required;
import io.mangoo.enums.Http;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;

import java.util.Objects;

public class ControllerRoute {
    private final Class<?> controllerClass;
    private boolean authentication;
    private boolean blocking;
    
    public ControllerRoute(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CONTROLLER_CLASS);
        
        controllerClass = clazz;
    }

    public void withRoutes(MangooRoute... routes) {
        Objects.requireNonNull(routes, Required.ROUTE);
        
        for (MangooRoute route : routes) {
            var requestRoute = (RequestRoute) route;
            requestRoute.withControllerClass(controllerClass);

            if (hasAuthentication()) {
                requestRoute.withAuthentication();
            }
            
            if (hasBlocking()) {
                requestRoute.withNonBlocking();
            }
            
            if (requestRoute.hasMultipleMethods()) {
                for (Http method : requestRoute.getMethods()) {
                    Router.addRoute(requestRoute.forMethod(method), method.name());
                }
            } else {
                Router.addRoute(requestRoute, ((RequestRoute) route).getMethod().name());
            }
        }
    }

    public ControllerRoute withAuthentication() {
        authentication = true;
        return this;
    }

    /** Executes the requests in a worker thread pool, so that long-running requests do not block the I/O thread. */
    public ControllerRoute withNonBlocking() {
        blocking = true;
        return this;
    }

    public boolean hasAuthentication() {
        return authentication;
    }

    public boolean hasBlocking() {
        return blocking;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }
}