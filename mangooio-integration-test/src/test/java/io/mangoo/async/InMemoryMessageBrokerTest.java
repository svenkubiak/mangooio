package io.mangoo.async;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class InMemoryMessageBrokerTest {

    @Test
    void testFailingSubscriberDoesNotStopOthers() {
        //given
        var broker = new InMemoryMessageBroker();
        List<String> received = new CopyOnWriteArrayList<>();
        broker.subscribe("topic", message -> {
            throw new IllegalStateException("failing subscriber");
        });
        broker.subscribe("topic", received::add);

        //when
        broker.publish("topic", "message");

        //then
        await().atMost(Duration.ofSeconds(5)).until(() -> received.size() == 1);
        assertThat(received, contains("message"));
    }

    @Test
    void testOtherTopicsAreNotDelivered() throws InterruptedException {
        //given
        var broker = new InMemoryMessageBroker();
        List<String> received = new CopyOnWriteArrayList<>();
        broker.subscribe("wanted", received::add);

        //when
        broker.publish("other", "message");
        Thread.sleep(200);

        //then
        assertThat(received, empty());
    }
}
