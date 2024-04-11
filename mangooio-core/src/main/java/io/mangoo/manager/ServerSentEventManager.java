package io.mangoo.manager;

import com.google.inject.Singleton;
import io.mangoo.enums.Required;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ServerSentEventManager {
    private static final Logger LOG = LogManager.getLogger(ServerSentEventManager.class);
    private static final Map<String, ServerSentEventConnection> CONNECTIONS = new ConcurrentHashMap<>(16, 0.9f, 1);

    public void addConnection(String uri, ServerSentEventConnection connection) {
        Objects.requireNonNull(uri, Required.URI.toString());
        Objects.requireNonNull(connection, Required.CONNECTION.toString());

        CONNECTIONS.put(uri, connection);
    }

    public void removeConnection(String uri) {
        Objects.requireNonNull(uri, Required.URI.toString());

        CONNECTIONS.remove(uri);
    }

    public void send(String uri, String data) {
        Objects.requireNonNull(uri, Required.URI.toString());

        Thread.ofVirtual().start(() -> {
            ServerSentEventConnection connection = CONNECTIONS.get(uri);
            if (connection != null) {
                if (connection.isOpen()) {
                    connection.send(data);
                } else {
                    LOG.error("ServerSentEvent connection for uri [{}] is closed", uri);
                }
            } else {
                LOG.error("ServerSentEvent connection for uri [{}] not found", uri);
            }
        });
    }
}
