package mangoo.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.undertow.util.StatusCodes;
import mangoo.io.testing.MangooRequest;
import mangoo.io.testing.MangooResponse;

import org.junit.Test;

public class ResourcesTest {
    
    @Test
    public void testResourceFile() {
        MangooResponse response = MangooRequest.get("/robots.txt").execute();

        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
    
    @Test
    public void testResourcePath() {
        MangooResponse response = MangooRequest.get("/assets/javascripts/jquery.min.js").execute();
        
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
    }
}