package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

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
public class JsonControllerTest {
    private static final String json = "{\"firstname\":\"Peter\",\"lastname\":\"Parker\",\"age\":24}";

    @Test
    public void testJsonSerialization() {
        //given
        TestResponse response = TestRequest.get("/render").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(json));
    }

    @Test
    public void testJsonParsingPost() {
        //given
        TestResponse response = TestRequest.post("/parse")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withStringBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Peter;Parker;24"));
    }
    
    @Test
    public void testJsonParsingPostConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            String json = "{\"firstname\":\"Peter\",\"lastname\":\"" + uuid + "\",\"age\":24}";
            
            //when
            TestResponse response = TestRequest.post("/parse")
                    .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                    .withStringBody(json)
                    .execute();

            // then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals("Peter;" + uuid + ";24");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    public void testJsonParsingPut() {
        //given
        TestResponse response = TestRequest.put("/parse")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withStringBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Peter;Parker;24"));
    }
    
    @Test
    public void testJsonParsingPatch() {
        //given
        TestResponse response = TestRequest.patch("/parse")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withStringBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Peter;Parker;24"));
    }

    @Test
    public void testJsonEmptyResponseBody() {
        //given
        TestResponse response = TestRequest.post("/body")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withStringBody("")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), not(nullValue()));
    }
    
    @Test
    public void testJsonNullResponseBody() {
        //given
        TestResponse response = TestRequest.post("/body")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withStringBody(null)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), not(nullValue()));
    }
    
    @Test
    public void testJsonResponseBody() {
        //given
        TestResponse response = TestRequest.post("/body")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withStringBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/body"));
    }

    @Test
    public void testJsonRequestBodyPost() {
        //given
        TestResponse response = TestRequest.post("/requestAndJson")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withStringBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/requestAndJsonPeter"));
    }
}