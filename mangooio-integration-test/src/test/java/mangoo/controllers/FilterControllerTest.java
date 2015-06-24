package mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.undertow.util.StatusCodes;
import mangoo.io.testing.MangooRequest;
import mangoo.io.testing.MangooResponse;

import org.junit.Test;

public class FilterControllerTest {

    @Test
    public void testContentFiler() {
        MangooResponse response = MangooRequest.get("/filter").execute();

        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());
        assertEquals("bar", response.getContent());
    }
}
