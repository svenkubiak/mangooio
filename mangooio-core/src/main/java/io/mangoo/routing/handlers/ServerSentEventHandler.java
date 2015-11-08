package io.mangoo.routing.handlers;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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
    private final String token;

    public ServerSentEventHandler(String token) {
        this.token = token;
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        if (StringUtils.isNotBlank(this.token)) {
            String uri = connection.getRequestURI();
            String queryString = connection.getQueryString();
            String header = "";
            if (connection.getRequestHeaders().get(Headers.AUTHORIZATION_STRING) != null) {
                header = connection.getRequestHeaders().get(Headers.AUTHORIZATION_STRING).element();
            }

            if (RequestUtils.hasValidAuthentication(uri, queryString, this.token, header)) {
                Application.getInstance(ServerEventManager.class).addConnection(connection);
            } else {
                IOUtils.closeQuietly(connection);
            }
        } else {
            Application.getInstance(ServerEventManager.class).addConnection(connection);
        }
    }
}