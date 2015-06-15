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
public class ParameterControllerTest {
    
    @Test
    public void stringTest() {
        MangooResponse response = MangooRequest.get("/string/bar").execute();
        
        assertNotNull(response.getContent());
        assertEquals("bar", response.getContent());
    }
    
    @Test
    public void doubleTest() {
        MangooResponse response = MangooRequest.get("/double/1.42").execute();
        
        assertNotNull(response.getContent());
        assertEquals("1.42", response.getContent());
    }
    
    @Test
    public void floatTest() {
        MangooResponse response = MangooRequest.get("/float/1.24").execute();
        
        assertNotNull(response.getContent());
        assertEquals("1.24", response.getContent());
    }
    
    @Test
    public void multipleTest() {
        MangooResponse response = MangooRequest.get("/multiple/bar/1").execute();
        
        assertNotNull(response.getContent());
        assertEquals("bar:1", response.getContent());
    }
    
    @Test
    public void pathTest() {
        MangooResponse response = MangooRequest.get("/path?foo=bar").execute();
        
        assertNotNull(response.getContent());
        assertEquals("bar", response.getContent());
    }
}