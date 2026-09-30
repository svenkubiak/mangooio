package handlers;

import io.mangoo.core.Application;
import io.mangoo.manager.ServerSentEventManager;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;

import java.util.Deque;
import java.util.Map;

/**
 * A custom connection callback which rejects a connection without a client query
 * parameter and holds every accepted one under that parameter instead of under the
 * request URI, so that an event can be sent to a single client
 */
public class ClientServerSentEventHandler implements ServerSentEventConnectionCallback {
    public static final String PARAMETER = "client";

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        String client = firstValue(connection.getQueryParameters(), PARAMETER);

        if (client == null) {
            connection.shutdown();
            return;
        }

        var manager = Application.getInstance(ServerSentEventManager.class);
        manager.addConnection(client, connection);

        // The connection is not held under its request URI, so ServerSentEventCloseListener
        // can not find it and the close task has to remove it under the same key
        connection.addCloseTask(closed -> manager.removeConnection(client, closed));
        connection.send(": ok\n\n");
    }

    private String firstValue(Map<String, Deque<String>> parameters, String name) {
        Deque<String> values = parameters.get(name);

        return values == null || values.isEmpty() ? null : values.peekFirst();
    }
}
