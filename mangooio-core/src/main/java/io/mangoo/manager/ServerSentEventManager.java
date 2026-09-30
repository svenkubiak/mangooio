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

    /**
     * Adds a connection under the given key. The default ServerSentEventHandler uses the
     * request URI of the connection as the key, so that every connection of a route is
     * addressed at once. A custom connection callback is free to use a key of its own,
     * e.g. one derived from a query parameter, to address a single client.
     *
     * @param key The key to hold the connection under
     * @param connection The connection to add
     */
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

    /**
     * Removes a connection which is held under its request URI. A connection that was
     * added under a key of its own has to be removed with
     * {@link #removeConnection(String, ServerSentEventConnection)}, as it can not be
     * found by its request URI.
     *
     * @param connection The connection to remove
     */
    public void removeConnection(ServerSentEventConnection connection) {
        Objects.requireNonNull(connection, Required.CONNECTION);

        removeConnection(connection.getRequestURI(), connection);
    }

    /**
     * Removes a connection from the given key
     *
     * @param key The key the connection was added under
     * @param connection The connection to remove
     */
    public void removeConnection(String key, ServerSentEventConnection connection) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(connection, Required.CONNECTION);

        SERVER_SENT_EVENT_CONNECTIONS.computeIfPresent(key, (k, connections) -> {
            connections.remove(connection);
            return connections.isEmpty() ? null : connections;
        });
    }

    /**
     * Sends data to every open connection held under the given key. Sending runs on a
     * virtual thread, so this method returns without waiting for the connections.
     *
     * @param key The key the connections are held under
     * @param data The data to send
     */
    public void send(String key, String data) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(data, Required.DATA);

        Thread.ofVirtual().start(() -> {
            for (ServerSentEventConnection connection : SERVER_SENT_EVENT_CONNECTIONS.getOrDefault(key, List.of())) {
                if (connection.isOpen()) {
                    // A connection can be closed between the check and the send, and a single
                    // failing connection must not cut the broadcast short for the remaining ones
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
