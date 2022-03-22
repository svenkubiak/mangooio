package listeners;

import java.util.UUID;

import com.google.common.eventbus.Subscribe;

import io.mangoo.events.ServerSentEventConnected;

public class ServerSentEventListener {
    @Subscribe
    public void foo(ServerSentEventConnected connection) throws InterruptedException {
        connection.getConnection().send(UUID.randomUUID().toString());
    }
}