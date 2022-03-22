package io.mangoo.events;

import java.util.Objects;

import io.mangoo.enums.Required;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 * 
 * @author svenkubiak
 *
 */
public class ServerSentEventDisconnected {
    private String uri;
    private ServerSentEventConnection connection;
    
    public ServerSentEventDisconnected(String uri, ServerSentEventConnection connection) {
        this.uri = Objects.requireNonNull(uri, Required.URI.toString());
        this.connection = Objects.requireNonNull(connection, Required.CONNECTION.toString());
    }

    public ServerSentEventConnection getConnection() {
        return connection;
    }
    
    public String getUri() {
        return uri;
    }
}