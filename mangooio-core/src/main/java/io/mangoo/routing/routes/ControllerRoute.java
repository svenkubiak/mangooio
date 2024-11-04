package io.mangoo.routing.routes;

import io.mangoo.constants.NotNull;
import io.mangoo.enums.Http;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;

import java.util.Objects;

public class ControllerRoute {
    private final Class<?> controllerClass;
    private boolean authentication;
    private boolean blocking;
    
    /**
     * Creates a new set of routes bind to a given controller class
     * 
     * @param clazz The controller class to bind to
     */
    public ControllerRoute(Class<?> clazz) {
        Objects.requireNonNull(clazz, NotNull.CONTROLLER_CLASS);
        
        controllerClass = clazz;
    }

    /**
     * Sets the given routes to the defined controller class
     * 
     * @param routes The routes to be configured for the controller
     */
    public void withRoutes(MangooRoute... routes) {
        Objects.requireNonNull(routes, NotNull.ROUTE);
        
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
                    requestRoute.withHttpMethod(method);
                    Router.addRoute(requestRoute, method.name());
                }
            } else {
                Router.addRoute(requestRoute, ((RequestRoute) route).getMethod().name());
            }
        }
    }

    /**
     * Sets authentication to true for all provided routes, default is false
     * 
     * @return controller route instance
     */
    public ControllerRoute withAuthentication() {
        authentication = true;
        return this;
    }

    /**
     * Configures this request as long-running request that is
     * executed in a different thread pool to not block the
     * non-blocking I/O request
     * 
     * @return ControllerRoute instance
     */
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