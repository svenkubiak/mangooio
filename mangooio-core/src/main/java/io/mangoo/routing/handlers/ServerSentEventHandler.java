package io.mangoo.routing.handlers;

import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.manager.ServerSentEventManager;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;

public class ServerSentEventHandler implements ServerSentEventConnectionCallback {
    private boolean hasAuthentication;
    
    public ServerSentEventHandler withAuthentication(boolean hasAuthentication) {
        this.hasAuthentication = hasAuthentication;
        return this;
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        if (hasAuthentication) {
            String header = null;
            var headerValues = connection.getRequestHeaders().get(Header.COOKIE.toHttpString());
            if (headerValues != null) {
                header = headerValues.element();
            }

            if (RequestUtils.hasValidAuthentication(header)) {
                Thread.ofVirtual().start(() -> Application.getInstance(ServerSentEventManager.class).addConnection(connection.getRequestURI(), connection));
                connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
            } else {
                MangooUtils.closeQuietly(connection);
            }
        } else {
            Thread.ofVirtual().start(() -> Application.getInstance(ServerSentEventManager.class).addConnection(connection.getRequestURI(), connection));
            connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
        }
    }
}