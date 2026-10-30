package io.mangoo.manager;

import io.mangoo.constants.Required;
import io.mangoo.interfaces.MessageBroker;
import io.mangoo.utils.JsonUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import jakarta.inject.Inject;
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
    private static final String TOPIC = "mangooio-sse";
    private final Map<String, List<ServerSentEventConnection>> connections = new ConcurrentHashMap<>();
    private final MessageBroker messageBroker;

    record Event(String key, String data) {
    }

    @Inject
    public ServerSentEventManager(MessageBroker messageBroker) {
        this.messageBroker = Objects.requireNonNull(messageBroker, "messageBroker can not be null");
        messageBroker.subscribe(TOPIC, json -> {
            Event event = JsonUtils.toObject(json, Event.class);
            if (event != null && event.key() != null && event.data() != null) {
                deliver(event.key(), event.data());
            }
        });
    }

    public ServerSentEventManager() {
        this.messageBroker = null;
    }

    public void addConnection(String key, ServerSentEventConnection connection) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(connection, Required.CONNECTION);

        connections.compute(key, (k, values) -> {
            if (values == null) {
                values = new CopyOnWriteArrayList<>();
            }
            values.add(connection);

            return values;
        });
    }

    public void removeConnection(ServerSentEventConnection connection) {
        Objects.requireNonNull(connection, Required.CONNECTION);

        removeConnection(connection.getRequestURI(), connection);
    }

    public void removeConnection(String key, ServerSentEventConnection connection) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(connection, Required.CONNECTION);

        connections.computeIfPresent(key, (k, values) -> {
            values.remove(connection);
            return values.isEmpty() ? null : values;
        });
    }

    public void send(String key, String data) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(data, Required.DATA);

        if (messageBroker == null) {
            deliver(key, data);
        } else {
            messageBroker.publish(TOPIC, JsonUtils.toJson(new Event(key, data)));
        }
    }

    private void deliver(String key, String data) {
        Thread.ofVirtual().start(() -> {
            for (ServerSentEventConnection connection : connections.getOrDefault(key, List.of())) {
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

    public void closeAll() {
        connections.values().forEach(values -> values.forEach(ServerSentEventConnection::shutdown));
    }
}
