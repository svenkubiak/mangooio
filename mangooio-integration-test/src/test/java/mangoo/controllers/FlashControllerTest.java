package mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.undertow.util.Methods;
import mangoo.io.testing.MangooRequest;
import mangoo.io.testing.MangooResponse;

import org.junit.Test;

public class FlashControllerTest {
    @Test
    public void formTest() {
        MangooResponse response = MangooRequest.instance().uri("/flash").method(Methods.GET).execute();

        assertNotNull(response);
        assertEquals("simpleerrorwarningsuccess", response.getContent());
    }
}