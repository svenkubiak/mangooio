package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.mangoo.enums.ContentType;
import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class JsonControllerTest {
    private static final String json = "{\"firstname\":\"Peter\",\"lastname\":\"Parker\",\"age\":24}";

    @Test
    public void renderTest() {
        MangooResponse response = MangooRequest.GET("/render").execute();

        assertNotNull(response.getContent());
        assertEquals(json, response.getContent());
    }

    @Test
    public void parseTest() {
        MangooResponse response = MangooRequest.POST("/parse").withContentType(ContentType.APPLICATION_JSON).withRequestBody(json).execute();

        assertNotNull(response.getContent());
        assertEquals("Peter;Parker;24", response.getContent());
    }

    @Test
    public void bodyTest() {
        MangooResponse response = MangooRequest.POST("/body").withContentType(ContentType.APPLICATION_JSON).withRequestBody("").execute();

        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertNotNull(response.getContent());

        response = MangooRequest.POST("/body").withContentType(ContentType.APPLICATION_JSON).withRequestBody(null).execute();

        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertNotNull(response.getContent());

        response = MangooRequest.POST("/body").withContentType(ContentType.APPLICATION_JSON).withRequestBody(json).execute();

        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertNotNull(response.getContent());
        assertEquals("/body", response.getContent());
    }

    @Test
    public void requestAndJsonTest() {
        MangooResponse response = MangooRequest.POST("/requestAndJson").withContentType(ContentType.APPLICATION_JSON).withRequestBody(json).execute();

        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertNotNull(response.getContent());
        assertEquals("/requestAndJsonPeter", response.getContent());
    }
}