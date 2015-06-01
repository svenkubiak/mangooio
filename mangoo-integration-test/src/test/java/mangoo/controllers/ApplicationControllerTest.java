package mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.undertow.util.StatusCodes;
import mangoo.io.testing.MangooRequest;
import mangoo.io.testing.MangooResponse;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class ApplicationControllerTest {
    
    @Test
    public void indexTest() {
        MangooResponse response = MangooRequest.get("/").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
    
    @Test
    public void indexTestWithContent() {
        MangooResponse response = MangooRequest.get("/").execute();
        
        assertNotNull(response);
        assertEquals("This is a test!", response.getContent());
    }
    
    @Test
    public void redirectTestWithoutRedirect() {
        MangooResponse response = MangooRequest.get("/redirect").disableRedirects(true).execute();

        assertNotNull(response);
        assertEquals(StatusCodes.FOUND, response.getStatusCode());
    }
    
    @Test
    public void redirectTestWithRedirect() {
        MangooResponse response = MangooRequest.get("/redirect").execute();
        
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
    
    @Test
    public void textTest() {
        MangooResponse response = MangooRequest.get("/text").execute();

        assertNotNull(response);
        assertEquals("text/plain; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
    
    @Test
    public void notFoundTest() {
        MangooResponse response = MangooRequest.get("/foo").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    public void forbiddenTest() {
        MangooResponse response = MangooRequest.get("/forbidden").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.FORBIDDEN, response.getStatusCode());
    }
    
    @Test
    public void badRequestTest() throws InterruptedException {
        MangooResponse response = MangooRequest.get("/badrequest").execute();
        
        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    public void unauthorizedTest() {
        MangooResponse response = MangooRequest.get("/unauthorized").execute();

        assertNotNull(response);
        assertEquals("text/html; charset=UTF-8", response.getContentType());
        assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
    }
}