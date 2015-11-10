package io.mangoo.routing.handlers;

import org.apache.commons.io.IOUtils;

import io.mangoo.core.Application;
import io.mangoo.managers.ServerEventManager;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.util.Headers;

/**
 *
 * @author svenkubiak
 *
 */
public class ServerSentEventHandler implements ServerSentEventConnectionCallback {
    private final boolean requiresAuthentication;

    public ServerSentEventHandler(boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        if (this.requiresAuthentication) {
            String header = null;
            if (connection.getRequestHeaders().get(Headers.SET_COOKIE) != null) {
                header = connection.getRequestHeaders().get(Headers.SET_COOKIE).element();
            }
            
            if (RequestUtils.hasValidAuthentication(header)) {
                Application.getInstance(ServerEventManager.class).addConnection(connection);
            } else {
                IOUtils.closeQuietly(connection);
            }
        } else {
            Application.getInstance(ServerEventManager.class).addConnection(connection);
        }
    }
}