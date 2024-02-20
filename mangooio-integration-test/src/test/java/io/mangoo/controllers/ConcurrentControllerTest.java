package io.mangoo.controllers;

import com.google.common.net.MediaType;
import io.mangoo.TestExtension;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class ConcurrentControllerTest {

    @Test
    void testConcurrentJsonParsing() throws InterruptedException {
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
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
}