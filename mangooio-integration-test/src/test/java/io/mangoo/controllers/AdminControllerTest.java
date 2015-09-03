package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.StatusCodes;

public class AdminControllerTest {

    @Test
    public void healthTest() {
        MangooResponse response = MangooRequest.get("/@health").execute();

        assertNotNull(response);
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertTrue(response.getContent().contains("alive"));
    }

    @Test
    public void configTest() {
        MangooResponse response = MangooRequest.get("/@config").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertTrue(response.getContent().contains("config"));
    }

    @Test
    public void routesTest() {
        MangooResponse response = MangooRequest.get("/@routes").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertTrue(response.getContent().contains("routes"));
    }

    @Test
    public void cacheTest() {
        MangooResponse response = MangooRequest.get("/@cache").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertTrue(response.getContent().contains("cache"));
    }
}