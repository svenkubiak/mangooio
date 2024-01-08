package io.mangoo.reactive;

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
class StreamTest {

    @Test
    void testSubscriber() {
        //given
        String uuid = UUID.randomUUID().toString();
        Stream stream = Application.getInstance(Stream.class);

        //when
        stream.publish(uuid);

        //then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(Utils.value.equals(uuid), equalTo(true)));
    }
}