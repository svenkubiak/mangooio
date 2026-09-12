package io.mangoo.routing.handlers;

import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.manager.ServerSentEventManager;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;

import java.util.Objects;

public class ServerSentEventHandler implements ServerSentEventConnectionCallback {

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        Objects.requireNonNull(connection, Required.CONNECTION);

        Runnable addConnectionTask = () -> {
            var serverEventManager = Application.getInstance(ServerSentEventManager.class);
            serverEventManager.addConnection(connection.getRequestURI(), connection);
            connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
            connection.send(": ok\n\n");
        };

        Thread.ofVirtual().start(addConnectionTask);
    }
}