package io.mangoo.events;

import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 * 
 * @author svenkubiak
 *
 */
public class ServerSentEventConnected {
    public ServerSentEventConnection connection;
    
    public ServerSentEventConnected(ServerSentEventConnection connection) {
        this.connection = connection;
    }

    public ServerSentEventConnection getConnection() {
        return connection;
    }
}