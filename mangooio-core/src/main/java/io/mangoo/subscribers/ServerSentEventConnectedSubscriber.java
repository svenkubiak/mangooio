package io.mangoo.subscribers;

import com.google.inject.Inject;
import io.mangoo.async.Subscriber;
import io.mangoo.events.ServerSentEventConnected;
import io.mangoo.manager.ServerSentEventManager;

import java.util.Objects;

public class ServerSentEventConnectedSubscriber implements Subscriber<ServerSentEventConnected> {
    private final ServerSentEventManager manager;

    @Inject
    public ServerSentEventConnectedSubscriber(ServerSentEventManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager can not be null");
    }

    @Override
    public void receive(ServerSentEventConnected event) {
        Objects.requireNonNull(event, "event can not be null");
        manager.addConnection(event.getUri(), event.getConnection());
    }
}
