package io.mangoo.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import io.mangoo.routing.bindings.Validator;
import io.mangoo.test.MangooTestInstance;

public class ValidatorTest {
    private Validator validator;
    private static final String URL = "url";
    private static final String REGEX = "regex";
    private static final String RANGE = "range";
    private static final String IPV6 = "ipv6";
    private static final String IPV4 = "ipv4";
    private static final String EMAIL = "email";
    private static final String MATCH2 = "match2";
    private static final String MATCH = "match";
    private static final String EXACT_MATCH = "exactMatch";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String REQUIRED = "required";
    private static final String CUSTOM_ERROR_MESSAGE = "Custom error message";

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
        validator.add(MATCH, MATCH);
        validator.match(MATCH, "foo");

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MATCH));
        assertEquals("match must match foo", validator.getError(MATCH));

        validator.add(MATCH, MATCH);
        validator.exactMatch(MATCH, "foo", CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(MATCH));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(MATCH));
    }

    @Test
    public void testMatchSuccess() {
        validator.add(MATCH, MATCH);
        validator.add(MATCH2, "mAtcH");
        validator.match(MATCH, MATCH2);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(MATCH));
    }

    @Test
    public void testEmailFailed() {
        validator.add(EMAIL, "foo @");
        validator.email(EMAIL);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(EMAIL));
        assertEquals("email must be a valid eMail address", validator.getError(EMAIL));

        validator.add(EMAIL, "foo @");
        validator.exactMatch(EMAIL, "foo", CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(EMAIL));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(EMAIL));
    }

    @Test
    public void testEmailSuccess() {
        validator.add(EMAIL, "foo@bar.com");
        validator.email(EMAIL);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(EMAIL));
    }

    @Test
    public void testIpv4Failed() {
        validator.add(IPV4, "192.189.383.122");
        validator.ipv4(IPV4);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(IPV4));
        assertEquals("ipv4 must be a valid IPv4 address", validator.getError(IPV4));

        validator.add(IPV4, "192.189.383.122");
        validator.ipv4(IPV4, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(IPV4));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(IPV4));
    }

    @Test
    public void testIpvSuccess() {
        validator.add(IPV4, "192.168.2.1");
        validator.ipv4(IPV4);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(IPV4));
    }

    @Test
    public void testIpv6Failed() {
        validator.add(IPV6, "1f::::0");
        validator.ipv6(IPV6);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(IPV6));
        assertEquals("ipv6 must be a valid IPv6 address", validator.getError(IPV6));

        validator.add(IPV6, "1f::::0");
        validator.ipv6(IPV6, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(IPV6));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(IPV6));
    }

    @Test
    public void testIpv6Success() {
        validator.add(IPV6, "2001:0db8:85a3:08d3:1319:8a2e:0370:7344");
        validator.ipv6(IPV6);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(IPV6));
    }

    @Test
    public void testRangeFailedString() {
        validator.add(RANGE, "abcdef");
        validator.range(RANGE, 8, 12);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(RANGE));
        assertEquals("range must have a size between 8 and 12", validator.getError(RANGE));

        validator.add(RANGE, "abcdef");
        validator.range(RANGE, 8, 12, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(RANGE));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(RANGE));
    }

    @Test
    public void testRangeFailedNumeric() {
        validator.add(RANGE, "4");
        validator.range(RANGE, 8, 12);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(RANGE));
        assertEquals("range must have a size between 8 and 12", validator.getError(RANGE));

        validator.add(RANGE, "4");
        validator.range(RANGE, 8, 12, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(RANGE));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(RANGE));
    }

    @Test
    public void testRangeSuccessString() {
        validator.add(RANGE, "abcdefg");
        validator.range(RANGE, 4, 10);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(RANGE));
    }

    @Test
    public void testRangeSuccessNumeric() {
        validator.add(RANGE, "6");
        validator.range(RANGE, 4, 10);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(RANGE));
    }

    @Test
    public void testRegexFailed() {
        validator.add(REGEX, "abc03");
        validator.regex(REGEX, Pattern.compile("[a-z]"));

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(REGEX));
        assertEquals("regex is invalid", validator.getError(REGEX));

        validator.add(REGEX, "abc03");
        validator.regex(REGEX, Pattern.compile("[a-z]"), CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(REGEX));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(REGEX));
    }

    @Test
    public void testRegexSuccess() {
        validator.add(REGEX, "abc");
        validator.regex(REGEX, Pattern.compile("^[A-Za-z0-9_.]+$"));

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(REGEX));
    }

    @Test
    public void testUrlFailed() {
        validator.add(URL, "http:/mangoo.io");
        validator.url(URL);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(URL));
        assertEquals("url must be a valid URL", validator.getError(URL));

        validator.add(URL, "http:/mangoo.io");
        validator.url(URL, CUSTOM_ERROR_MESSAGE);

        assertTrue(validator.hasErrors());
        assertTrue(validator.hasError(URL));
        assertEquals(CUSTOM_ERROR_MESSAGE, validator.getError(URL));
    }

    @Test
    public void testUrlSuccess() {
        validator.add(URL, "https://mangoo.io");
        validator.url(URL);

        assertFalse(validator.hasErrors());
        assertFalse(validator.hasError(URL));
    }
}