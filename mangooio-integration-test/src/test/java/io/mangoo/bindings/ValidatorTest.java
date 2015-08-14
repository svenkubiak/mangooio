package io.mangoo.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.mangoo.routing.bindings.Validator;
import io.mangoo.test.MangooTestInstance;

public class ValidatorTest {
    private static final String EXACT_MATCH = "exactMatch";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String REQUIRED = "required";
    private static final String CUSTOM_ERROR_MESSAGE = "Custom error message";
    private Validator validator;

    @Before
    public void setup () {
        validator = MangooTestInstance.IO.getInjector().getInstance(Validator.class);
    }

    @Test
    public void testRequiredFailed() {
        validator.add(REQUIRED, "");
        validator.required(REQUIRED);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(REQUIRED));
        assertEquals("required is required", validator.getError(REQUIRED));

        validator.add(REQUIRED, "");
        validator.required(REQUIRED, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(REQUIRED));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(REQUIRED));
    }

    @Test
    public void testRequiredSuccess() {
        validator.add(REQUIRED, REQUIRED);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(REQUIRED));
    }

    @Test
    public void testMinFailedString() {
        validator.add(MIN, "abcdef");
        validator.min(MIN, 8);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MIN));
        assertEquals("min must have a least a size of 8", validator.getError(MIN));

        validator.add(MIN, "abcdef");
        validator.min(MIN, 8, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MIN));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(MIN));
    }

    @Test
    public void testMinFailedNumeric() {
        validator.add(MIN, "4");
        validator.min(MIN, 8);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MIN));
        assertEquals("min must have a least a size of 8", validator.getError(MIN));

        validator.add(MIN, "4");
        validator.min(MIN, 8, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MIN));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(MIN));
    }

    @Test
    public void testMinSuccessString() {
        validator.add(MIN, "abcdefg");
        validator.min(MIN, 4);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(MIN));
    }

    @Test
    public void testMinSuccessNumeric() {
        validator.add(MIN, "6");
        validator.min(MIN, 4);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(MIN));
    }

    @Test
    public void testMaxFailedString() {
        validator.add(MAX, "abcdef");
        validator.max(MAX, 3);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MAX));
        assertEquals("max must have a size of max 3", validator.getError(MAX));

        validator.add(MAX, "abcdef");
        validator.max(MAX, 3, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MAX));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(MAX));
    }

    @Test
    public void testMaxFailedNumeric() {
        validator.add(MAX, "4");
        validator.max(MAX, 2);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MAX));
        assertEquals("max must have a size of max 2", validator.getError(MAX));

        validator.add(MAX, "4");
        validator.max(MAX, 2, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MAX));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(MAX));
    }

    @Test
    public void testMaxSuccessString() {
        validator.add(MAX, "abcdef");
        validator.max(MAX, 10);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(MAX));
    }

    @Test
    public void testMaxSuccessNumeric() {
        validator.add(MAX, "3");
        validator.max(MAX, 4);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(MAX));
    }

    @Test
    public void testExactMatchFailed() {
        validator.add(EXACT_MATCH, EXACT_MATCH);
        validator.exactMatch(EXACT_MATCH, "exactmatch");

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(EXACT_MATCH));
        assertEquals("exactMatch must exactly match exactmatch", validator.getError(EXACT_MATCH));

        validator.add(EXACT_MATCH, EXACT_MATCH);
        validator.exactMatch(EXACT_MATCH, "exactmatch", CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(EXACT_MATCH));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(EXACT_MATCH));
    }

    @Test
    public void testExactMatchSuccess() {
        validator.add(EXACT_MATCH, EXACT_MATCH);
        validator.exactMatch(EXACT_MATCH, EXACT_MATCH);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(EXACT_MATCH));
    }

    @Test
    public void testMatchFailed() {
        validator.add("match", "match");
        validator.match("match", "foo");

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError("match"));
        assertEquals("match must match foo", validator.getError("match"));

        validator.add("match", "match");
        validator.exactMatch("match", "foo", CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError("match"));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError("match"));
    }

    @Test
    public void testMatchSuccess() {
        validator.add("match", "match");
        validator.match("match", "mAtcH");

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError("match"));
    }
}