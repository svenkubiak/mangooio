package io.mangoo.routing.listeners;

import io.mangoo.async.EventBus;
import io.mangoo.core.Application;
import io.mangoo.enums.Queue;
import io.mangoo.events.ServerSentEventDisconnected;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import org.xnio.ChannelListener;

public class ServerSentEventCloseListener implements ChannelListener<ServerSentEventConnection> {
    @SuppressWarnings("unchecked")
    @Override
    public void handleEvent(ServerSentEventConnection connection) {
        Application.getInstance(EventBus.class).publish(Queue.SSE.toString(), new ServerSentEventDisconnected(connection.getRequestURI(), connection));
    }
}