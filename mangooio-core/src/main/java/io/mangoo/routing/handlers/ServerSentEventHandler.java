package io.mangoo.routing.handlers;

import io.mangoo.constants.Default;
import io.mangoo.constants.Header;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.manager.ServerSentEventManager;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;

import java.util.Objects;

public class ServerSentEventHandler implements ServerSentEventConnectionCallback {

    /** Disables proxy buffering and caching and sends a heartbeat comment, so proxies like nginx neither buffer events nor close the connection. */
    public static HttpHandler wrap(ServerSentEventConnectionCallback callback) {
        Objects.requireNonNull(callback, "callback can not be null");

        HttpHandler serverSentEvents = Handlers.serverSentEvents((connection, lastEventId) -> {
            connection.setKeepAliveTime(Default.SERVER_SENT_EVENT_KEEP_ALIVE);
            callback.connected(connection, lastEventId);
        });

        // Undertow flushes the response headers before the callback runs, so they have to be set here
        return exchange -> {
            exchange.getResponseHeaders().put(Header.CACHE_CONTROL, "no-cache");
            exchange.getResponseHeaders().put(Header.X_ACCEL_BUFFERING, "no");
            serverSentEvents.handleRequest(exchange);
        };
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        Objects.requireNonNull(connection, Required.CONNECTION);

        Runnable addConnectionTask = () -> {
            var serverEventManager = Application.getInstance(ServerSentEventManager.class);
            serverEventManager.addConnection(connection.getRequestURI(), connection);
            connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
        };

        Thread.ofVirtual().start(addConnectionTask);
    }
}