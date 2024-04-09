package io.mangoo.subscribers;

import com.google.inject.Inject;
import io.mangoo.async.Subscriber;
import io.mangoo.events.ServerSentEventDisconnected;
import io.mangoo.manager.ServerSentEventManager;

import java.util.Objects;

public class ServerSentEventDisconnectedSubscriber implements Subscriber<ServerSentEventDisconnected> {
    private final ServerSentEventManager manager;

    @Inject
    public ServerSentEventDisconnectedSubscriber(ServerSentEventManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager can not be null");
    }

    @Override
    public void receive(ServerSentEventDisconnected event) {
        manager.removeConnection(event.getUri());
    }
}
