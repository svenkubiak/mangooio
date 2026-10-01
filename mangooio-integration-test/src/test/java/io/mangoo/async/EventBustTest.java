package io.mangoo.async;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import subscribers.FailingEvent;
import subscribers.FailingEventWitnessSubscriber;
import subscribers.HelperFirstEvent;
import subscribers.HelperFirstSubscriber;
import subscribers.NestedEventSubscriber;
import utils.Utils;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@ExtendWith({TestExtension.class})
class EventBustTest {

    @Test
    @SuppressWarnings("unchecked")
    void testEventBus() {
        //given
        String uuid = UUID.randomUUID().toString();
        EventBus eventBus = Application.getInstance(EventBus.class);

        //when
        eventBus.publish(uuid);

        //then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(uuid.equals(Utils.eventBusValue), equalTo(true)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSubscriberWithHelperMethodBeforeReceive() {
        //given
        String uuid = UUID.randomUUID().toString();

        //when
        Application.getInstance(EventBus.class).publish(new HelperFirstEvent(uuid));

        //then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(HelperFirstSubscriber.value, equalTo(uuid)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNestedEventType() {
        //given
        String uuid = UUID.randomUUID().toString();

        //when
        Application.getInstance(EventBus.class).publish(new NestedEventSubscriber.Event(uuid));

        //then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(NestedEventSubscriber.value, equalTo(uuid)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFailingSubscriberDoesNotBlockOtherSubscribers() {
        //given
        String uuid = UUID.randomUUID().toString();

        //when
        Application.getInstance(EventBus.class).publish(new FailingEvent(uuid));

        //then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(FailingEventWitnessSubscriber.value, equalTo(uuid)));
    }
}
