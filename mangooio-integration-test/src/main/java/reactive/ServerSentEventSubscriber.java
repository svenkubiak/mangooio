package reactive;

import io.mangoo.async.Subscriber;
import io.mangoo.events.ServerSentEventConnected;

import java.util.UUID;

public class ServerSentEventSubscriber implements Subscriber<ServerSentEventConnected> {

    @Override
    public void receive(ServerSentEventConnected connection) {
        connection.getConnection().send(UUID.randomUUID().toString());
    }
}