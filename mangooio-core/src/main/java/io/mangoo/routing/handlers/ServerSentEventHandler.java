package io.mangoo.routing.handlers;

import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.events.ServerSentEventConnected;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.mangoo.services.EventBusService;
import io.mangoo.utils.MangooUtils;
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
        if (hasAuthentication) {
            String header = null;
            HeaderValues headerValues = connection.getRequestHeaders().get(Header.COOKIE.toHttpString());
            if (headerValues != null) {
                header = headerValues.element();
            }

            if (RequestUtils.hasValidAuthentication(header)) {
                Application.getInstance(EventBusService.class).publish(new ServerSentEventConnected(connection.getRequestURI(), connection));
            } else {
                MangooUtils.closeQuietly(connection);
            }
        } else {
            connection.addCloseTask(Application.getInstance(ServerSentEventCloseListener.class));
            Application.getInstance(EventBusService.class).publish(new ServerSentEventConnected(connection.getRequestURI(), connection));
        }
    }
}