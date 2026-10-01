package io.mangoo.routing.routes;

import io.mangoo.constants.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;

import java.util.Objects;

public class ServerSentEventRoute implements MangooRoute {
    private String url;
    private Class<? extends ServerSentEventConnectionCallback> handler;

    public ServerSentEventRoute to(String url) {
        Objects.requireNonNull(url, Required.URL);

        if ('/' != url.charAt(0)) {
            url = "/" + url;
        }
        this.url = url;

        Router.addRoute(this, "sse");

        return this;
    }

    /**
     * Replaces the default handler, which registers the connection in the ServerSentEventManager under the request URI of the route.
     * A custom handler must register the connection itself and attach a close task that removes it again; ServerSentEventCloseListener only works for request URI keys.
     */
    public ServerSentEventRoute withHandler(Class<? extends ServerSentEventConnectionCallback> handler) {
        this.handler = handler;

        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public Class<? extends ServerSentEventConnectionCallback> getHandler() {
        return handler;
    }
}
