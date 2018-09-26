package io.mangoo.routing.routes;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

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
    private boolean authentication;
    private String username;
    private String password;
    
    /**
     * Creates a new set of route to a given controller class
     * 
     * @param clazz The controller class to bind to
     */
    public ControllerRoute(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CONTROLLER_CLASS.toString());
        
        this.controllerClass = clazz;
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
            requestRoute.withControllerClass(this.controllerClass);

            if (hasBasicAuthentication()) {
                requestRoute.withBasicAuthentication(this.username, this.password);
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
     * Sets Basic HTTP authentication to all method on the defined controller class
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
     * Sets authentication to true, default is false
     * 
     * @return controller route instance
     */
    public ControllerRoute requireAuthentication() {
        this.authentication = true;
        
        return this;
    }
    
    /**
     * Sets required authorization role to this route
     * Authorization implicitly set authentication to true
     * 
     * @param role The require role
     * @return controller route instance
     */
    public ControllerRoute requireAuthorization(String role) {
        // TODO Implementation needed
        
        return this;
    }
    
    public boolean hasAuthentication() {
        return this.authentication;
    }
    
    public boolean hasBasicAuthentication() {
        return StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password);
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