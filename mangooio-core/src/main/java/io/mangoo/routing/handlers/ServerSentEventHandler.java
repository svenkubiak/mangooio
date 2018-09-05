package io.mangoo.routing.handlers;

import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.mangoo.services.ServerSentEventService;
import io.mangoo.utils.IOUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.util.HeaderValues;

/**
 *
 * @author svenkubiak
 *
 */
public class ServerSentEventHandler implements ServerSentEventConnectionCallback {
    private boolean hasAuthentication;
    
    public ServerSentEventHandler withAuthentication(boolean hasAuthentication) {
        this.hasAuthentication = hasAuthentication;
        return this;
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        if (this.hasAuthentication) {
            String header = null;
            HeaderValues headerValues = connection.getRequestHeaders().get(Header.COOKIE.toHttpString());
            if (headerValues != null) {
                header = headerValues.element();
            }

            if (RequestUtils.hasValidAuthentication(header)) {
                Application.getInstance(ServerSentEventService.class).addConnection(connection);
                connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
            } else {
                IOUtils.closeQuietly(connection);
            }
        } else {
            Application.getInstance(ServerSentEventService.class).addConnection(connection);
            connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
        }
    }
}