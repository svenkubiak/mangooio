package io.mangoo.routing.routes;

import io.mangoo.constants.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;
import io.undertow.websockets.WebSocketConnectionCallback;

import java.util.Objects;

public class WebSocketRoute implements MangooRoute {
    private String url;
    private Class<? extends WebSocketConnectionCallback> handler;

    public WebSocketRoute to(String url) {
        Objects.requireNonNull(url, Required.URL);

        if ('/' != url.charAt(0)) {
            url = "/" + url;
        }
        this.url = url;

        Router.addRoute(this, "websocket");

        return this;
    }

    public WebSocketRoute withHandler(Class<? extends WebSocketConnectionCallback> handler) {
        this.handler = handler;

        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public Class<? extends WebSocketConnectionCallback> getHandler() {
        return handler;
    }
}


