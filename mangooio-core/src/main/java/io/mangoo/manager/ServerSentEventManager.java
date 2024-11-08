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

        SERVER_SENT_EVENT_CONNECTIONS
                .computeIfAbsent(uri, key -> new ArrayList<>())
                .add(connection);
    }

    public void removeConnection(ServerSentEventConnection connection) {
        Objects.requireNonNull(connection, NotNull.CONNECTION);
        String uri = connection.getRequestURI();

        SERVER_SENT_EVENT_CONNECTIONS.computeIfPresent(uri, (key, connections) -> {
            connections.remove(connection);
            return connections.isEmpty() ? null : connections;
        });
    }

    public void send(String uri, String data) {
        Objects.requireNonNull(uri, NotNull.URI);
        Objects.requireNonNull(uri, NotNull.DATA);

        Thread.ofVirtual().start(() ->
                SERVER_SENT_EVENT_CONNECTIONS
                        .getOrDefault(uri, List.of())
                        .stream()
                        .filter(ServerSentEventConnection::isOpen)
                        .forEach(connection -> connection.send(data))
        );
    }
}
