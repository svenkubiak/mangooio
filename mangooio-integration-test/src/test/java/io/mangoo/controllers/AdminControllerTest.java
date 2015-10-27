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
        MangooResponse response = MangooRequest.GET("/@health").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
        
        response = MangooRequest.GET("/@health").withBasicauthentication("admin", "admin").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertTrue(response.getContent().contains("alive"));
    }

    @Test
    public void configTest() {
        MangooResponse response = MangooRequest.GET("/@config").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
        
        response = MangooRequest.GET("/@config").withBasicauthentication("admin", "admin").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertTrue(response.getContent().contains("config"));
    }

    @Test
    public void routesTest() {
        MangooResponse response = MangooRequest.GET("/@routes").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
        
        response = MangooRequest.GET("/@routes").withBasicauthentication("admin", "admin").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertTrue(response.getContent().contains("routes"));
    }

    @Test
    public void cacheTest() {
        MangooResponse response = MangooRequest.GET("/@cache").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());

        response = MangooRequest.GET("/@cache").withBasicauthentication("admin", "admin").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertTrue(response.getContent().contains("cache"));
    }

    @Test
    public void metricsTest() {
        MangooResponse response = MangooRequest.GET("/@metrics").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());

        response = MangooRequest.GET("/@metrics").withBasicauthentication("admin", "admin").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertTrue(response.getContent().contains("metrics"));
    }
    
    @Test
    public void schedulerTest() {
        MangooResponse response = MangooRequest.GET("/@scheduler").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());

        response = MangooRequest.GET("/@scheduler").withBasicauthentication("admin", "admin").execute();
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertTrue(response.getContent().contains("scheduler"));
    }
}