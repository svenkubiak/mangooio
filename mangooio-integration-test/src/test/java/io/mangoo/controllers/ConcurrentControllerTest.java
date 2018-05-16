package io.mangoo.controllers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import com.google.common.net.MediaType;

import io.mangoo.TestSuite;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class ConcurrentControllerTest {

    @Test
    public void testConcurrentJsonParsing() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            String json = "{\"firstname\":\"$$\",\"lastname\":\"Parker\",\"age\":24}";
            json = json.replace("$$", uuid);
            
            WebResponse response = WebRequest.post("/parse")
                    .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                    .withRequestBody(json)
                    .execute();
            
            // then
            return response.getStatusCode() == StatusCodes.OK && response.getContent().equals(uuid + ";Parker;24");
        }, new org.llorllale.cactoos.matchers.RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
}