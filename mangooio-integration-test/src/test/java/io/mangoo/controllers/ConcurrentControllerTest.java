package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;

import org.junit.Test;

import io.mangoo.enums.ContentType;
import io.mangoo.test.utils.ConcurrentRunner;
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
        Runnable runnable = () -> {
            for (int j=0; j < 50; j++) {
                //given
                String uuid = UUID.randomUUID().toString();
                String json = "{\"firstname\":\"$$\",\"lastname\":\"Parker\",\"age\":24}";
                json = json.replace("$$", uuid);
                
                WebResponse response = WebRequest.post("/parse")
                        .withContentType(ContentType.APPLICATION_JSON)
                        .withRequestBody(json)
                        .execute();

                //then
                assertThat(response, not(nullValue()));
                assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
                assertThat(response.getContent(), equalTo(uuid + ";Parker;24"));    
            }
        };
        
        ConcurrentRunner.create()
            .withRunnable(runnable)
            .withThreads(50)
            .run();
    }
}