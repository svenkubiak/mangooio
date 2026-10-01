package io.mangoo.routing;

import io.mangoo.routing.routes.*;

public class Bind {
    
    private Bind() {
    }

    public static WebSocketRoute webSocket() {
        return new WebSocketRoute();
    }

    public static ServerSentEventRoute serverSentEvent() {
        return new ServerSentEventRoute();
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