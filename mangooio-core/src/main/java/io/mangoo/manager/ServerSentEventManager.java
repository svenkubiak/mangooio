package io.mangoo.manager;

import com.google.inject.Singleton;
import io.mangoo.constants.NotNull;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ServerSentEventManager {
    private static final Map<String, List<ServerSentEventConnection>> SERVER_SENT_EVENT_CONNECTIONS = new ConcurrentHashMap<>(16, 0.9f, 1);

    public void addConnection(String uri, ServerSentEventConnection connection) {
        Objects.requireNonNull(uri, NotNull.URI);
        Objects.requireNonNull(connection, NotNull.CONNECTION);

        List<ServerSentEventConnection> connections = SERVER_SENT_EVENT_CONNECTIONS.get(uri);
        if (connections == null) {
            connections = new ArrayList<>();
        }
        connections.add(connection);

        SERVER_SENT_EVENT_CONNECTIONS.put(uri, connections);
    }

    public void removeConnection(ServerSentEventConnection connection) {
        Objects.requireNonNull(connection, NotNull.CONNECTION);
        String uri = connection.getRequestURI();

        List<ServerSentEventConnection> connections = SERVER_SENT_EVENT_CONNECTIONS.get(uri);
        if (connections != null && !connections.isEmpty()) {
            connections.remove(connection);
            SERVER_SENT_EVENT_CONNECTIONS.put(uri, connections);
        }

        if (connections != null && connections.isEmpty()) {
            SERVER_SENT_EVENT_CONNECTIONS.remove(uri);
        }
    }

    public void send(String uri, String data) {
        Objects.requireNonNull(uri, NotNull.URI);

        Thread.ofVirtual().start(() -> {
            List<ServerSentEventConnection> connections = SERVER_SENT_EVENT_CONNECTIONS.get(uri);
            if (connections != null && !connections.isEmpty()) {
                for (ServerSentEventConnection connection : connections) {
                    if (connection.isOpen()) {
                        connection.send(data);
                    }
                }
            }
        });
    }
}
