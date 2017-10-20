package io.mangoo.routing.handlers;

import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.helpers.RequestHelper;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.mangoo.services.ServerSentEventService;
import io.mangoo.utils.IOUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.util.HeaderValues;

/**
 *
 * @author svenkubiak
 *
 */
public class ServerSentEventHandler implements ServerSentEventConnectionCallback {
    private final RequestHelper requestHelper;
    private boolean hasAuthentication;
    
    @Inject
    public ServerSentEventHandler(RequestHelper requestHelper) {
        this.requestHelper = Objects.requireNonNull(requestHelper, Required.REQUEST_HELPER.toString());
    }
    
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

            if (this.requestHelper.hasValidAuthentication(header)) {
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