package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;

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
        
        response = MangooRequest.get("/string/tüsätö").execute();

        assertNotNull(response.getContent());
        assertEquals("tüsätö", response.getContent());
    }

    @Test
    public void doublePrimitiveTest() {
        MangooResponse response = MangooRequest.get("/doublePrimitive/1.42").execute();

        assertNotNull(response.getContent());
        assertEquals("1.42", response.getContent());
    }

    @Test
    public void doubleTest() {
        MangooResponse response = MangooRequest.get("/double/1.42").execute();

        assertNotNull(response.getContent());
        assertEquals("1.42", response.getContent());
    }

    @Test
    public void intTest() {
        MangooResponse response = MangooRequest.get("/int/42").execute();

        assertNotNull(response.getContent());
        assertEquals("42", response.getContent());
    }

    @Test
    public void integerTest() {
        MangooResponse response = MangooRequest.get("/integer/42").execute();

        assertNotNull(response.getContent());
        assertEquals("42", response.getContent());
    }

    @Test
    public void floatTest() {
        MangooResponse response = MangooRequest.get("/float/1.24").execute();

        assertNotNull(response.getContent());
        assertEquals("1.24", response.getContent());
    }

    @Test
    public void floatPrimitiveTest() {
        MangooResponse response = MangooRequest.get("/floatPrimitive/1.24").execute();

        assertNotNull(response.getContent());
        assertEquals("1.24", response.getContent());
    }

    @Test
    public void longTest() {
        MangooResponse response = MangooRequest.get("/long/60000").execute();

        assertNotNull(response.getContent());
        assertEquals("60000", response.getContent());
    }

    @Test
    public void longPrimitiveTest() {
        MangooResponse response = MangooRequest.get("/longPrimitive/6000").execute();

        assertNotNull(response.getContent());
        assertEquals("6000", response.getContent());
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