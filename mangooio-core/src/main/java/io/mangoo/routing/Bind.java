package io.mangoo.routing;

import io.mangoo.routing.routes.ControllerRoute;
import io.mangoo.routing.routes.FileRoute;
import io.mangoo.routing.routes.PathRoute;
import io.mangoo.routing.routes.ServerSentEventRoute;
import io.mangoo.routing.routes.WebSocketRoute;

public class Bind {
    public static ServerSentEventRoute serverSentEvent() {
        return new ServerSentEventRoute();
    }

    public static WebSocketRoute webSocket() {
        return new WebSocketRoute();
    }

    public static PathRoute pathResource() {
        return new PathRoute();
    }

    public static FileRoute fileResource() {
        return new FileRoute();
    }
    
    public static ControllerRoute controller(Class<?> clazz) {
        return new ControllerRoute(clazz);
    }
}