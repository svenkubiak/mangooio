package io.mangoo.routing.handlers;

import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.manager.ServerSentEventManager;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;

import java.util.Objects;

public class ServerSentEventHandler implements ServerSentEventConnectionCallback {
    private boolean hasAuthentication;
    
    public ServerSentEventHandler withAuthentication(boolean hasAuthentication) {
        this.hasAuthentication = hasAuthentication;
        return this;
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        Objects.requireNonNull(connection, NotNull.CONNECTION);

        Runnable addConnectionTask = () -> {
            var serverEventManager = Application.getInstance(ServerSentEventManager.class);
            serverEventManager.addConnection(connection.getRequestURI(), connection);
            connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
        };

        if (hasAuthentication) {
            var headerValues = connection.getRequestHeaders().get(Header.COOKIE);
            var header = headerValues != null ? headerValues.element() : null;

            if (RequestUtils.hasValidAuthentication(header)) {
                Thread.ofVirtual().start(addConnectionTask);
            } else {
                MangooUtils.closeQuietly(connection);
            }
        } else {
            Thread.ofVirtual().start(addConnectionTask);
        }

    }
}