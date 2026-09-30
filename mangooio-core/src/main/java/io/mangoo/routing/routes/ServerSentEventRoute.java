package io.mangoo.routing.routes;

import io.mangoo.constants.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;

import java.util.Objects;

public class ServerSentEventRoute implements MangooRoute {
    private String url;
    private Class<? extends ServerSentEventConnectionCallback> handler;

    /**
     * Sets the URL for this route
     *
     * @param url The URL for this route
     * @return ServerSentEventRoute instance
     */
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
     * Sets an optional connection callback for this route which replaces the default
     * ServerSentEventHandler. Without a handler the connection is registered in the
     * ServerSentEventManager under the request URI of the route and can be addressed
     * through ServerSentEventManager#send(uri, data), which sends to every connection
     * of that URI.
     *
     * <p>A custom handler takes over that work entirely. It is responsible for
     * registering the connection - or for deliberately not registering it, e.g. when
     * it rejects the connection - and for attaching a close task that removes the
     * connection again. A handler that registers the connection under the request URI
     * can keep using ServerSentEventCloseListener as that close task, as the listener
     * removes a connection by its request URI. A handler that uses a key of its own has
     * to attach a close task which calls
     * ServerSentEventManager#removeConnection(key, connection) with that key.</p>
     *
     * <p>A custom handler is the way to address a single client, as it can inspect
     * the connection while it is being established, e.g. its query parameters or
     * headers, and register it under a key of its own choosing.</p>
     *
     * @param handler The connection callback class for this route
     * @return ServerSentEventRoute instance
     */
    public ServerSentEventRoute withHandler(Class<? extends ServerSentEventConnectionCallback> handler) {
        this.handler = handler;

        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    /**
     * @return The connection callback class of this route or null if the default handler is used
     */
    public Class<? extends ServerSentEventConnectionCallback> getHandler() {
        return handler;
    }
}
