package io.mangoo.manager;

import io.mangoo.constants.Required;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class ServerSentEventManager {
    private static final Logger LOG = LogManager.getLogger(ServerSentEventManager.class);

    /**
     * Connections are held in a CopyOnWriteArrayList, as sending is read-dominated and
     * iterates without holding the map lock. All mutations run inside the compute block of
     * the map, so that adding and removing a connection can not interleave.
     */
    private static final Map<String, List<ServerSentEventConnection>> SERVER_SENT_EVENT_CONNECTIONS = new ConcurrentHashMap<>();

    public void addConnection(String uri, ServerSentEventConnection connection) {
        Objects.requireNonNull(uri, Required.URI);
        Objects.requireNonNull(connection, Required.CONNECTION);

        SERVER_SENT_EVENT_CONNECTIONS.compute(uri, (key, connections) -> {
            List<ServerSentEventConnection> values =
                    connections == null ? new CopyOnWriteArrayList<>() : connections;
            values.add(connection);

            return values;
        });
    }

    public void removeConnection(ServerSentEventConnection connection) {
        Objects.requireNonNull(connection, Required.CONNECTION);
        String uri = connection.getRequestURI();

        SERVER_SENT_EVENT_CONNECTIONS.computeIfPresent(uri, (key, connections) -> {
            connections.remove(connection);
            return connections.isEmpty() ? null : connections;
        });
    }

    public void send(String uri, String data) {
        Objects.requireNonNull(uri, Required.URI);
        Objects.requireNonNull(data, Required.DATA);

        Thread.ofVirtual().start(() -> {
            for (ServerSentEventConnection connection : SERVER_SENT_EVENT_CONNECTIONS.getOrDefault(uri, List.of())) {
                if (connection.isOpen()) {
                    // A connection can be closed between the check and the send, and a single
                    // failing connection must not cut the broadcast short for the remaining ones
                    try {
                        connection.send(data);
                    } catch (RuntimeException e) {
                        LOG.error("Failed to send server sent event to a connection on '{}'", uri, e);
                    }
                }
            }
        });
    }
}
