package io.mangoo.routing.listeners;

import io.mangoo.core.Application;
import io.mangoo.manager.ServerSentEventManager;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import org.xnio.ChannelListener;

public class ServerSentEventCloseListener implements ChannelListener<ServerSentEventConnection> {
    @Override
    public void handleEvent(ServerSentEventConnection connection) {
        Thread.ofVirtual().start(() -> Application.getInstance(ServerSentEventManager.class).removeConnection(connection));
    }
}