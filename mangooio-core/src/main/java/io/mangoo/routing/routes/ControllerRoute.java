package io.mangoo.routing.routes;

import io.mangoo.enums.Http;
import io.mangoo.enums.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 
 * @author svenkubiak
 *
 */
public class ControllerRoute {
    private final Class<?> controllerClass;
    private String username;
    private String password;
    private boolean authentication;
    private boolean authorization;
    private boolean blocking;
    
    /**
     * Creates a new set of routes bind to a given controller class
     * 
     * @param clazz The controller class to bind to
     */
    public ControllerRoute(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CONTROLLER_CLASS.toString());
        
        controllerClass = clazz;
    }

    /**
     * Sets the given routes to the defined controller class
     * 
     * @param routes The routes to be configured for the controller
     */
    public void withRoutes(MangooRoute... routes) {
        Objects.requireNonNull(routes, Required.ROUTE.toString());
        
        for (MangooRoute route : routes) {
            RequestRoute requestRoute = (RequestRoute) route;
            requestRoute.withControllerClass(controllerClass);

            if (hasBasicAuthentication()) {
                requestRoute.withBasicAuthentication(username, password);
            }
            
            if (hasAuthentication()) {
                requestRoute.withAuthentication();
            }
            
            if (hasBlocking()) {
                requestRoute.withNonBlocking();
            }
            
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

    /**
     * Sets Basic HTTP authentication to all method on the given controller class
     * 
     * @param username The username for basic authentication in clear text
     * @param password The password for basic authentication in clear text
     * 
     * @return controller route instance
     */
    public ControllerRoute withBasicAuthentication(String username, String password) {
        Objects.requireNonNull(username, Required.USERNAME.toString());
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        
        this.username = username;
        this.password = password;
        
        return this;
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
     * Sets authentication to true for all provided routes, default is false
     * Also sets authentication to true, default is false
     * 
     * @return controller route instance
     */
    public ControllerRoute withAuthorization() {
        authorization = true;
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
    
    public boolean hasAuthorization() {
        return authorization;
    }
    
    public boolean hasBlocking() {
        return blocking;
    }
    
    public boolean hasBasicAuthentication() {
        return StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password);
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}