package mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import mangoo.io.testing.MangooRequest;
import mangoo.io.testing.MangooResponse;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthenticityControllerTest {
    
    @Test
    public void formTest() {
        MangooResponse response = MangooRequest.get("/authenticityform").execute();

        assertNotNull(response);
        assertTrue(response.getContent().startsWith("<input type=\"hidden\" value=\""));
        assertTrue(response.getContent().endsWith(" name=\"authenticityToken\" />"));
    }
    
    @Test
    public void tokenTest() {
        MangooResponse response = MangooRequest.get("/authenticitytoken").execute();
        
        assertNotNull(response.getContent());
        assertEquals(16, response.getContent().length());
    }
    
    @Test
    public void validTest() {
        MangooResponse instance = MangooRequest.instance();
        
        MangooResponse response = instance.uri("/authenticitytoken").method(Methods.GET).execute();
        String token = response.getContent();
        assertNotNull(token);
        assertEquals(16, token.length());

        response = instance.uri("/valid?authenticityToken=" + token).method(Methods.GET).execute();
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("bar", response.getContent());
    }
    
    @Test
    public void invalidTest() {
        MangooResponse response = MangooRequest.get("/authenticitytoken").execute();
        assertNotNull(response.getContent());
        assertEquals(16, response.getContent().length());

        response = MangooRequest.get("/invalid?authenticityToken=fdjsklfjsd82jkfldsjkl").execute();
        assertEquals(StatusCodes.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getContent().contains("bar"));
    }
}