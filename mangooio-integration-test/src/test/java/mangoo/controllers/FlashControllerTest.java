package mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.undertow.util.Methods;
import mangoo.io.test.MangooBrowser;
import mangoo.io.test.MangooResponse;

public class FlashControllerTest {
    @Test
    public void formTest() {
        MangooResponse response = MangooBrowser.getInstance().uri("/flash").method(Methods.GET).execute();

        assertNotNull(response);
        assertEquals("simpleerrorwarningsuccess", response.getContent());
    }
}