package io.mangoo.events;

import io.mangoo.enums.Required;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

import java.util.Objects;

public class ServerSentEventConnected {
    private final String uri;
    private final ServerSentEventConnection connection;
    
    public ServerSentEventConnected(String uri, ServerSentEventConnection connection) {
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