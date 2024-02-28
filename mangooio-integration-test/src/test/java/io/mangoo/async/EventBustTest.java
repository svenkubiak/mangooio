package io.mangoo.async;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class EventBustTest {
    public static String value = null;

    @Test
    void testEventBus() {
        //given
        String uuid = UUID.randomUUID().toString();
        EventBus eventBus = Application.getInstance(EventBus.class);

        //when
        eventBus.register("foo", MySubscriber.class);
        eventBus.publish("foo", uuid);

        //then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(EventBustTest.value.equals(uuid), equalTo(true)));
    }
}