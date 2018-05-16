package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.llorllale.cactoos.matchers.RunsInThreads;

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
public class JsonControllerTest {
    private static final String json = "{\"firstname\":\"Peter\",\"lastname\":\"Parker\",\"age\":24}";

    @Test
    public void testJsonSerialization() {
        //given
        WebResponse response = WebRequest.get("/render").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(json));
    }

    @Test
    public void testJsonParsingPost() {
        //given
        WebResponse response = WebRequest.post("/parse")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withRequestBody(json)
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
            WebResponse response = WebRequest.post("/parse")
                    .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                    .withRequestBody(json)
                    .execute();

            // then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals("Peter;" + uuid + ";24");
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
    
    @Test
    public void testJsonParsingPut() {
        //given
        WebResponse response = WebRequest.put("/parse")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withRequestBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Peter;Parker;24"));
    }
    
    @Test
    public void testJsonParsingPatch() {
        //given
        WebResponse response = WebRequest.patch("/parse")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withRequestBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Peter;Parker;24"));
    }

    @Test
    public void testJsonEmptyResponseBody() {
        //given
        WebResponse response = WebRequest.post("/body")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withRequestBody("")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), not(nullValue()));
    }
    
    @Test
    public void testJsonNullResponseBody() {
        //given
        WebResponse response = WebRequest.post("/body")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withRequestBody(null)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), not(nullValue()));
    }
    
    @Test
    public void testJsonResponseBody() {
        //given
        WebResponse response = WebRequest.post("/body")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withRequestBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/body"));
    }

    @Test
    public void testJsonRequestBodyPost() {
        //given
        WebResponse response = WebRequest.post("/requestAndJson")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withRequestBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/requestAndJsonPeter"));
    }
}