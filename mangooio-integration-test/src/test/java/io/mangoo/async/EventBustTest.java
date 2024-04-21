package io.mangoo.async;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import utils.Utils;

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
}