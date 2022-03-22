package io.mangoo.events;

import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 * 
 * @author svenkubiak
 *
 */
public class ServerSentEventDisconnected {
    public ServerSentEventConnection connection;
    
    public ServerSentEventDisconnected(ServerSentEventConnection connection) {
        this.connection = connection;
    }

    public ServerSentEventConnection getConnection() {
        return connection;
    }
}