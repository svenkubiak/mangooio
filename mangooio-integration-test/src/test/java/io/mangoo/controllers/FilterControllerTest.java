package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

public class FilterControllerTest {

    @Test
    public void testContentFiler() {
        MangooResponse response = MangooRequest.GET("/filter").execute();

        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("bar", response.getContent());
    }

    @Test
    public void testHeaderFilter() {
        MangooResponse response = MangooRequest.GET("/headerfilter").execute();

        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("12", response.getHttpResponse().getFirstHeader(Headers.CONTENT_MD5_STRING).getValue());
    }
}