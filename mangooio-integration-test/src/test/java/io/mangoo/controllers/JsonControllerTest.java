package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.enums.ContentType;
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
    public void testJsonParsing() {
        //given
        WebResponse response = WebRequest.post("/parse")
                .withContentType(ContentType.APPLICATION_JSON)
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
                .withContentType(ContentType.APPLICATION_JSON)
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
                .withContentType(ContentType.APPLICATION_JSON)
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
                .withContentType(ContentType.APPLICATION_JSON)
                .withRequestBody(json)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/body"));
    }

    @Test
    public void testJsonRequestBody() {
        //given
        WebResponse response = WebRequest.post("/requestAndJson")
                .withContentType(ContentType.APPLICATION_JSON)
                .withRequestBody(json)
                .execute();

        //then
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/requestAndJsonPeter"));
    }
}