package io.mangoo.persistence;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.interfaces.MessageBroker;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.manager.ServerSentEventManager;
import io.mangoo.routing.handlers.ServerSentEventHandler;
import io.mangoo.utils.CommonUtils;
import io.undertow.Undertow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Two brokers with their own instance id on one collection stand in for two instances
@ExtendWith({TestExtension.class})
class MongoMessageBrokerTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static Datastore datastore() {
        return Application.getInstance(Datastore.class);
    }

    private static String collection() {
        return "test_messages_" + CommonUtils.uuidV6().replace("-", "");
    }

    private static MongoMessageBroker broker(String collection, String instance) {
        var broker = new MongoMessageBroker(datastore(), collection, instance);
        broker.prepare();
        broker.start();
        return broker;
    }

    @Test
    void testBindingUsesMongoDBWithPersistence() {
        //then
        assertThat(Application.getInstance(MessageBroker.class), instanceOf(MongoMessageBroker.class));
        assertThat(((MongoMessageBroker) Application.getInstance(MessageBroker.class)).isRunning(), equalTo(true));
    }

    @Test
    void testMessageReachesTheOtherInstance() {
        //given
        String collection = collection();
        var first = broker(collection, "first");
        var second = broker(collection, "second");
        List<String> atFirst = new CopyOnWriteArrayList<>();
        List<String> atSecond = new CopyOnWriteArrayList<>();
        first.subscribe("topic", atFirst::add);
        second.subscribe("topic", atSecond::add);

        try {
            //when
            first.publish("topic", "from-first");
            second.publish("topic", "from-second");

            //then every instance receives both messages exactly once
            await().atMost(TIMEOUT).until(() -> atFirst.size() == 2 && atSecond.size() == 2);
            await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3)).until(() -> atFirst.size() == 2 && atSecond.size() == 2);
            assertThat(atFirst, containsInAnyOrder("from-first", "from-second"));
            assertThat(atSecond, containsInAnyOrder("from-first", "from-second"));
        } finally {
            first.stop();
            second.stop();
        }
    }

    @Test
    void testOtherTopicsAreNotDelivered() {
        //given
        String collection = collection();
        var first = broker(collection, "first");
        var second = broker(collection, "second");
        List<String> received = new CopyOnWriteArrayList<>();
        second.subscribe("wanted", received::add);

        try {
            //when
            first.publish("other", "ignored");
            first.publish("wanted", "delivered");

            //then
            await().atMost(TIMEOUT).until(() -> received.size() == 1);
            assertThat(received, contains("delivered"));
        } finally {
            first.stop();
            second.stop();
        }
    }

    @Test
    void testMessagesBeforeTheStartAreNotReplayed() {
        //given
        String collection = collection();
        var first = broker(collection, "first");
        first.publish("topic", "before");
        var second = new MongoMessageBroker(datastore(), collection, "second");
        List<String> received = new CopyOnWriteArrayList<>();
        second.subscribe("topic", received::add);

        try {
            //when
            second.prepare();
            second.start();
            first.publish("topic", "after");

            //then
            await().atMost(TIMEOUT).until(() -> received.size() == 1);
            assertThat(received, contains("after"));
        } finally {
            first.stop();
            second.stop();
        }
    }

    @Test
    void testTailingResumesAfterTheCollectionWasDropped() {
        //given
        String collection = collection();
        var first = broker(collection, "first");
        var second = broker(collection, "second");
        List<String> received = new CopyOnWriteArrayList<>();
        second.subscribe("topic", received::add);

        try {
            //when
            datastore().getMongoDatabase().getCollection(collection).drop();
            await().atMost(Duration.ofSeconds(30)).until(() -> isCapped(collection));
            first.publish("topic", "after-reconnect");

            //then
            await().atMost(Duration.ofSeconds(30)).until(() -> received.contains("after-reconnect"));
        } finally {
            first.stop();
            second.stop();
        }
    }

    @Test
    void testPrepareCreatesACappedCollectionWithSeed() {
        //given
        String collection = collection();

        //when
        new MongoMessageBroker(datastore(), collection, "first").prepare();

        //then
        assertThat(isCapped(collection), equalTo(true));
        assertThat(datastore().getMongoDatabase().getCollection(collection).countDocuments(), equalTo(1L));
    }

    @Test
    void testPrepareFailsForACollectionThatIsNotCapped() {
        //given
        String collection = collection();
        datastore().getMongoDatabase().createCollection(collection);

        //then
        var exception = assertThrows(IllegalStateException.class, () -> new MongoMessageBroker(datastore(), collection, "first").prepare());
        assertThat(exception.getMessage(), containsString("not capped"));
    }

    @Test
    void testPublishWithoutDatabaseStillDeliversLocally() {
        //given
        var closed = new DatastoreImpl(Application.getInstance(io.mangoo.core.Config.class));
        closed.close();
        var broker = new MongoMessageBroker(closed, collection(), "first");
        List<String> received = new CopyOnWriteArrayList<>();
        broker.subscribe("topic", received::add);

        //when
        broker.publish("topic", "local");

        //then
        await().atMost(TIMEOUT).until(() -> received.size() == 1);
    }

    @Test
    void testServerSentEventReachesAClientOfTheOtherInstance() throws Exception {
        //given an SSE client connected to the second instance
        String collection = collection();
        var first = broker(collection, "first");
        var second = broker(collection, "second");
        var sender = new ServerSentEventManager(first);
        var receiver = new ServerSentEventManager(second);
        var connected = new CountDownLatch(1);
        var undertow = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(ServerSentEventHandler.wrap((connection, lastEventId) -> {
                    receiver.addConnection("/events", connection);
                    connected.countDown();
                }))
                .build();
        undertow.start();

        try {
            var port = ((InetSocketAddress) undertow.getListenerInfo().getFirst().getAddress()).getPort();
            CompletableFuture<HttpResponse<InputStream>> response = HttpClient.newHttpClient().sendAsync(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/events")).build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            assertThat(connected.await(5, TimeUnit.SECONDS), equalTo(true));
            var reader = new BufferedReader(new InputStreamReader(response.get(5, TimeUnit.SECONDS).body(), StandardCharsets.UTF_8));

            //when the first instance sends
            sender.send("/events", "across-instances");

            //then
            CompletableFuture<String> line = CompletableFuture.supplyAsync(() -> {
                try {
                    String current;
                    while ((current = reader.readLine()) != null) {
                        if (current.startsWith("data:")) {
                            return current;
                        }
                    }
                    return null;
                } catch (Exception e) {
                    return null;
                }
            });
            assertThat(line.get(10, TimeUnit.SECONDS), equalTo("data:across-instances"));
            response.cancel(true);
        } finally {
            receiver.closeAll();
            undertow.stop();
            first.stop();
            second.stop();
        }
    }

    private static boolean isCapped(String collection) {
        var options = datastore().getMongoDatabase().listCollections()
                .filter(com.mongodb.client.model.Filters.eq("name", collection)).first();
        return options != null && Boolean.TRUE.equals(options.get("options", org.bson.Document.class).getBoolean("capped"));
    }
}
