package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
/**
 * 
 * @author svenkubiak
 *
 */
public class I18nControllerTest {
    @Test
    public void templateTest() {
        MangooResponse response = MangooRequest.GET("/translation").withHeader("Accept-Language", "de-DE").execute();
        
        assertNotNull(response.getContent());
        assertEquals("willkommen", response.getContent());
    }
}