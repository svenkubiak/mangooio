package io.mangoo.routing.handlers;

import org.apache.commons.io.IOUtils;

import io.mangoo.core.Application;
import io.mangoo.managers.ServerEventManager;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

/**
 *
 * @author svenkubiak
 *
 */
public class ServerSentEventHandler implements ServerSentEventConnectionCallback {
    private final boolean hasAuthentication;

    public ServerSentEventHandler(boolean hasAuthentication) {
        this.hasAuthentication = hasAuthentication;
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        if (this.hasAuthentication) {
            String header = null;
            HeaderValues headerValues = connection.getRequestHeaders().get(Headers.COOKIE);
            if (headerValues != null) {
                header = headerValues.element();
            }

            if (RequestUtils.hasValidAuthentication(header)) {
                Application.getInstance(ServerEventManager.class).addConnection(connection);
                connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
            } else {
                IOUtils.closeQuietly(connection);
            }
        } else {
            Application.getInstance(ServerEventManager.class).addConnection(connection);
            connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
        }
    }
}