package io.mangoo.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.StatusCodes;

import org.junit.Test;

public class ResourcesTest {
    
    @Test
    public void testResourceFile() {
        MangooResponse response = MangooRequest.GET("/robots.txt").execute();

        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
    
    @Test
    public void testResourcePath() {
        MangooResponse response = MangooRequest.GET("/assets/javascripts/jquery.min.js").execute();
        
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
}