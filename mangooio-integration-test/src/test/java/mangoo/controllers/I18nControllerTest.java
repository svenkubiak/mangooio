package mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import mangoo.io.testing.MangooRequest;
import mangoo.io.testing.MangooResponse;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class I18nControllerTest {

    @Test
    public void templateTest() {
        MangooResponse response = MangooRequest.get("/translation").header("Accept-Language", "de-DE").execute();
        
        assertNotNull(response.getContent());
        assertEquals("willkommen", response.getContent());
    }
}