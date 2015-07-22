package io.mangoo.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.mangoo.routing.bindings.Flash;

public class FlashTest {

    @Test
    public void testSuccess() {
        Flash flash = new Flash();
        flash.setSuccess("success");

        assertEquals("success", flash.get("success"));
    }

    @Test
    public void testWarning() {
        Flash flash = new Flash();
        flash.setWarning("warning");

        assertEquals("warning", flash.get("warning"));
    }

    @Test
    public void testError() {
        Flash flash = new Flash();
        flash.setError("error");

        assertEquals("error", flash.get("error"));
    }

    @Test
    public void testContent() {
        Flash flash = new Flash();
        assertFalse(flash.hasContent());

        flash.add("foo", "bar");
        assertTrue(flash.hasContent());
    }
}