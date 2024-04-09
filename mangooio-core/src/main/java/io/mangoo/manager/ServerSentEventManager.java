package io.mangoo.manager;

import com.google.inject.Singleton;
import io.mangoo.enums.Required;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
public class ServerSentEventManager {
    private final Map<String, ServerSentEventConnection> connections = new HashMap<>();

    public void addConnection(String url, ServerSentEventConnection connection) {
        Objects.requireNonNull(url, Required.URL.toString());
        Objects.requireNonNull(connection, Required.CONNECTION.toString());

        connections.put(url, connection);
    }

    public void removeConnection(String url) {
        Objects.requireNonNull(url, Required.URL.toString());

        connections.remove(url);
    }

    public void send(String url, String data) {
        ServerSentEventConnection connection = connections.get(url);
        if (connection != null && connection.isOpen()) {
            connection.send(data);
        }
    }
}
