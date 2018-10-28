package io.mangoo.controllers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.net.MediaType;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class ConcurrentControllerTest {

    @Test
    public void testConcurrentJsonParsing() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            String json = "{\"firstname\":\"$$\",\"lastname\":\"Parker\",\"age\":24}";
            json = json.replace("$$", uuid);
            
            TestResponse response = TestRequest.post("/parse")
                    .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                    .withStringBody(json)
                    .execute();
            
            // then
            return response.getStatusCode() == StatusCodes.OK && response.getContent().equals(uuid + ";Parker;24");
        }, new org.llorllale.cactoos.matchers.RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
}