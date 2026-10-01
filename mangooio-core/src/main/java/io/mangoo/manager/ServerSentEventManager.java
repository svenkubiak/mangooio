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

    // CopyOnWriteArrayList as sending is read-dominated; all mutations run inside compute, so adding and removing can not interleave.
    private static final Map<String, List<ServerSentEventConnection>> SERVER_SENT_EVENT_CONNECTIONS = new ConcurrentHashMap<>();

    /** The default handler uses the request URI as key; a custom connection callback may use a key of its own to address a single client. */
    public void addConnection(String key, ServerSentEventConnection connection) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(connection, Required.CONNECTION);

        SERVER_SENT_EVENT_CONNECTIONS.compute(key, (k, connections) -> {
            List<ServerSentEventConnection> values =
                    connections == null ? new CopyOnWriteArrayList<>() : connections;
            values.add(connection);

            return values;
        });
    }

    /** Only finds connections held under their request URI; use {@link #removeConnection(String, ServerSentEventConnection)} for a custom key. */
    public void removeConnection(ServerSentEventConnection connection) {
        Objects.requireNonNull(connection, Required.CONNECTION);

        removeConnection(connection.getRequestURI(), connection);
    }

    public void removeConnection(String key, ServerSentEventConnection connection) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(connection, Required.CONNECTION);

        SERVER_SENT_EVENT_CONNECTIONS.computeIfPresent(key, (k, connections) -> {
            connections.remove(connection);
            return connections.isEmpty() ? null : connections;
        });
    }

    /** Sends on a virtual thread and returns without waiting for the connections. */
    public void send(String key, String data) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(data, Required.DATA);

        Thread.ofVirtual().start(() -> {
            for (ServerSentEventConnection connection : SERVER_SENT_EVENT_CONNECTIONS.getOrDefault(key, List.of())) {
                if (connection.isOpen()) {
                    // A connection can close between check and send, and one failing connection must not abort the broadcast.
                    try {
                        connection.send(data);
                    } catch (RuntimeException e) {
                        LOG.error("Failed to send server sent event to a connection on '{}'", key, e);
                    }
                }
            }
        });
    }
}
