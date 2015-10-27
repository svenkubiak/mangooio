package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.mangoo.test.MangooBrowser;
import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthenticityControllerTest {
    
    @Test
    public void formTest() {
        MangooResponse response = MangooRequest.GET("/authenticityform").execute();

        assertNotNull(response);
        assertTrue(response.getContent().startsWith("<input type=\"hidden\" value=\""));
        assertTrue(response.getContent().endsWith(" name=\"authenticityToken\" />"));
    }
    
    @Test
    public void tokenTest() {
        MangooResponse response = MangooRequest.GET("/authenticitytoken").execute();
        
        assertNotNull(response.getContent());
        assertEquals(16, response.getContent().length());
    }
    
    @Test
    public void validTest() {
    	MangooBrowser instance = MangooBrowser.open();
        
        MangooResponse response = instance.withUri("/authenticitytoken").withMethod(Methods.GET).execute();
        String token = response.getContent();
        assertNotNull(token);
        assertEquals(16, token.length());
        
        response = instance.withUri("/valid?authenticityToken=" + token).withMethod(Methods.GET).execute();
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("bar", response.getContent());
    }
    
    @Test
    public void invalidTest() {
        MangooResponse response = MangooRequest.GET("/authenticitytoken").execute();
        assertNotNull(response.getContent());
        assertEquals(16, response.getContent().length());

        response = MangooRequest.GET("/invalid?authenticityToken=fdjsklfjsd82jkfldsjkl").execute();
        assertEquals(StatusCodes.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getContent().contains("bar"));
    }
}