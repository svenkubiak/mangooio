package reactive;

import io.mangoo.async.MangooSubscriber;
import io.mangoo.events.ServerSentEventConnected;

import java.util.UUID;

public class ServerSentEventSubscriber implements MangooSubscriber<ServerSentEventConnected> {

    @Override
    public void receive(ServerSentEventConnected connection) {
        connection.getConnection().send(UUID.randomUUID().toString());
    }
}