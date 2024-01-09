package listeners;

import io.mangoo.events.ServerSentEventConnected;
import io.mangoo.reactive.Subscriber;

import java.util.UUID;

public class ServerSentEventListener extends Subscriber<ServerSentEventConnected> {
    @Override
    public void onNext(ServerSentEventConnected connection) {
        connection.getConnection().send(UUID.randomUUID().toString());
    }
}