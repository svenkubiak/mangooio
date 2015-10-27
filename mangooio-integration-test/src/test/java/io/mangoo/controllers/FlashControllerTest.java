package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.mangoo.test.MangooBrowser;
import io.mangoo.test.MangooResponse;
import io.undertow.util.Methods;

public class FlashControllerTest {
    @Test
    public void formTest() {
        MangooResponse response = MangooBrowser.open().withUri("/flash").withMethod(Methods.GET).execute();

        assertNotNull(response);
        assertEquals("simpleerrorwarningsuccess", response.getContent());
    }
}