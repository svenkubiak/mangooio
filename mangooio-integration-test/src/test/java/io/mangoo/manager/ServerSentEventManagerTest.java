package io.mangoo.manager;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class ServerSentEventManagerTest {
    private static final int CONNECTIONS = 200;
    private static final int THREADS = 16;
    private static final String BROADCAST = "broadcast";
    private static final String CHURN = "churn";

    private ServerSentEventConnection connection(String uri) {
        var connection = Mockito.mock(ServerSentEventConnection.class);
        when(connection.getRequestURI()).thenReturn(uri);
        when(connection.isOpen()).thenReturn(true);

        return connection;
    }

    @Test
    void testConcurrentAddsAreNotLost() throws Exception {
        //given
        var manager = Application.getInstance(ServerSentEventManager.class);
        var uri = "/sse-concurrent-adds";
        List<ServerSentEventConnection> connections = new ArrayList<>();
        List<Callable<Void>> tasks = new ArrayList<>();

        for (var i = 0; i < CONNECTIONS; i++) {
            var connection = connection(uri);
            connections.add(connection);
            tasks.add(() -> {
                manager.addConnection(uri, connection);
                return null;
            });
        }

        //when
        invokeAll(tasks);
        manager.send(uri, BROADCAST);

        //then no connection may get lost by a concurrent add
        try {
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                for (ServerSentEventConnection connection : connections) {
                    Mockito.verify(connection, Mockito.atLeastOnce()).send(BROADCAST);
                }
            });
        } finally {
            connections.forEach(manager::removeConnection);
        }
    }

    @Test
    void testConcurrentChurnWhileBroadcasting() throws Exception {
        //given
        var manager = Application.getInstance(ServerSentEventManager.class);
        var uri = "/sse-churn";
        List<ServerSentEventConnection> kept = new CopyOnWriteArrayList<>();
        List<ServerSentEventConnection> removed = new CopyOnWriteArrayList<>();
        List<Callable<Void>> tasks = new ArrayList<>();

        for (var i = 0; i < CONNECTIONS; i++) {
            var connection = connection(uri);
            boolean keep = i % 2 != 0;

            tasks.add(() -> {
                manager.addConnection(uri, connection);

                // Removing every second connection repeatedly empties the list, which is when
                // an add outside of the compute block can end up on a detached list
                if (keep) {
                    kept.add(connection);
                } else {
                    manager.removeConnection(connection);
                    removed.add(connection);
                }

                // Broadcasting while the list mutates iterates it concurrently
                manager.send(uri, CHURN);
                return null;
            });
        }

        //when
        invokeAll(tasks);
        manager.send(uri, BROADCAST);

        //then
        try {
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                for (ServerSentEventConnection connection : kept) {
                    Mockito.verify(connection, Mockito.atLeastOnce()).send(BROADCAST);
                }
            });

            for (ServerSentEventConnection connection : removed) {
                Mockito.verify(connection, Mockito.never()).send(BROADCAST);
            }
        } finally {
            kept.forEach(manager::removeConnection);
        }
    }

    private void invokeAll(List<Callable<Void>> tasks) throws Exception {
        var executor = Executors.newFixedThreadPool(THREADS);
        try {
            for (Future<Void> future : executor.invokeAll(tasks)) {
                future.get();
            }
        } finally {
            executor.shutdown();
        }
    }
}
