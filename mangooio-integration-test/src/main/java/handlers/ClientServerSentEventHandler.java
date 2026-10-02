package handlers;

import io.mangoo.core.Application;
import io.mangoo.manager.ServerSentEventManager;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;

import java.util.Deque;
import java.util.Map;

/** Rejects connections without a client query parameter and holds accepted ones under that parameter instead of the request URI, so an event can be sent to a single client. */
public class ClientServerSentEventHandler implements ServerSentEventConnectionCallback {
    public static final String PARAMETER = "client";
    public static volatile long keepAliveTime; //NOSONAR

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        keepAliveTime = connection.getKeepAliveTime();
        String client = firstValue(connection.getQueryParameters(), PARAMETER);

        if (client == null) {
            connection.shutdown();
            return;
        }

        var manager = Application.getInstance(ServerSentEventManager.class);
        manager.addConnection(client, connection);

        // Not held under its request URI, so ServerSentEventCloseListener cannot find it and the close task has to remove it under the client key.
        connection.addCloseTask(closed -> manager.removeConnection(client, closed));
    }

    private String firstValue(Map<String, Deque<String>> parameters, String name) {
        Deque<String> values = parameters.get(name);

        return values == null || values.isEmpty() ? null : values.peekFirst();
    }
}
