package io.mangoo.routing;

import io.mangoo.routing.routes.ControllerRoute;
import io.mangoo.routing.routes.FileRoute;
import io.mangoo.routing.routes.PathRoute;
import io.mangoo.routing.routes.ServerSentEventRoute;
import io.mangoo.routing.routes.WebSocketRoute;

/**
 * 
 * @author svenkubiak
 *
 */
public class Bind {
    
    private Bind() {
    }
    
    /**
     * Creates a new ServerSentEvent route
     * 
     * @return ServerSentEventRoute instance
     */
    public static ServerSentEventRoute serverSentEvent() {
        return new ServerSentEventRoute();
    }

    /**
     * Creates a new WebSocke route
     * 
     * @return WebSocketRoute instance
     */
    public static WebSocketRoute webSocket() {
        return new WebSocketRoute();
    }

    /**
     * Creates a new PathResource route
     * 
     * @return PathRoute instance
     */
    public static PathRoute pathResource() {
        return new PathRoute();
    }

    /**
     * Create a new FileResource route
     * 
     * @return FileRoute instance
     */
    public static FileRoute fileResource() {
        return new FileRoute();
    }
    
    /**
     * Creates a new controller route for binding multiple routes
     * 
     * @param clazz The controller class
     * @return ControllerRoute instance
     */
    public static ControllerRoute controller(Class<?> clazz) {
        return new ControllerRoute(clazz);
    }
}