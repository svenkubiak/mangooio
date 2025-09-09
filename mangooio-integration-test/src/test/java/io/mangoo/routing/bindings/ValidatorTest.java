package io.mangoo.routing.bindings;

import com.google.re2j.Pattern;
import io.mangoo.TestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({TestExtension.class})
class ValidatorTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = new Validator();
    }

    @Test
    void testGetWithValidKey() {
        // Given
        String key = "testKey";
        String expectedValue = "testValue";
        validator.addValue(key, expectedValue);

        // When
        String result = validator.get(key);

        // Then
        assertEquals(expectedValue, result);
    }

    @Test
    void testGetWithNonExistentKey() {
        // Given
        String key = "nonExistentKey";

        // When
        String result = validator.get(key);

        // Then
        assertNull(result);
    }

    @Test
    void testGetWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.get(key));
    }

    @Test
    void testAddValue() {
        // Given
        String key = "testKey";
        String value = "testValue";

        // When
        validator.addValue(key, value);

        // Then
        assertEquals(value, validator.get(key));
    }

    @Test
    void testHasErrorsInitiallyFalse() {
        // When
        boolean result = validator.hasErrors();

        // Then
        assertFalse(result);
    }

    @Test
    void testHasErrorsWithErrors() {
        // Given
        validator.addValue("testKey", "testValue");
        validator.expectValue("testKey");

        // When
        boolean result = validator.hasErrors();

        // Then
        assertFalse(result); // expectValue should not add error for valid value
    }

    @Test
    void testHasErrorsWithActualErrors() {
        // Given
        validator.expectValue("nonExistentKey");

        // When
        boolean result = validator.hasErrors();

        // Then
        assertTrue(result);
    }

    @Test
    void testGetErrorWithExistingError() {
        // Given
        validator.expectValue("nonExistentKey");

        // When
        String result = validator.getError("nonExistentKey");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetErrorWithNonExistentError() {
        // Given
        String key = "nonExistentKey";

        // When
        String result = validator.getError(key);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetErrorWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.getError(key));
    }

    @Test
    void testHasErrorWithExistingError() {
        // Given
        validator.expectValue("nonExistentKey");

        // When
        boolean result = validator.hasError("nonExistentKey");

        // Then
        assertTrue(result);
    }

    @Test
    void testHasErrorWithNonExistentError() {
        // Given
        String key = "nonExistentKey";

        // When
        boolean result = validator.hasError(key);

        // Then
        assertFalse(result);
    }

    @Test
    void testHasErrorWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.hasError(key));
    }

    @Test
    void testGetErrors() {
        // Given
        validator.expectValue("key1");
        validator.expectValue("key2");

        // When
        Map<String, String> result = validator.getErrors();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("key1"));
        assertTrue(result.containsKey("key2"));
    }

    @Test
    void testIsValidInitiallyTrue() {
        // When
        boolean result = validator.isValid();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsValidWithErrors() {
        // Given
        validator.expectValue("nonExistentKey");

        // When
        boolean result = validator.isValid();

        // Then
        assertFalse(result);
    }

    @Test
    void testInvalidate() {
        // Given
        assertTrue(validator.isValid());

        // When
        validator.invalidate();

        // Then
        assertFalse(validator.isValid());
        assertTrue(validator.hasErrors());
    }

    @Test
    void testExpectValueWithValidValue() {
        // Given
        String key = "testKey";
        String value = "testValue";
        validator.addValue(key, value);

        // When
        validator.expectValue(key);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectValueWithBlankValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "   ");

        // When
        validator.expectValue(key);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectValueWithEmptyValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "");

        // When
        validator.expectValue(key);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectValueWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectValue(key));
    }

    @Test
    void testExpectValueWithCustomMessage() {
        // Given
        String key = "testKey";
        String customMessage = "Custom error message";
        validator.addValue(key, "");

        // When
        validator.expectValue(key, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectMinValueWithValidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "10");

        // When
        validator.expectMinValue(key, 5.0);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectMinValueWithInvalidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "3");

        // When
        validator.expectMinValue(key, 5.0);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectMinValueWithNonNumericValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "invalid");

        // When
        validator.expectMinValue(key, 5.0);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectMinValueWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectMinValue(key, 5.0));
    }

    @Test
    void testExpectMinValueWithCustomMessage() {
        // Given
        String key = "testKey";
        String customMessage = "Custom min value error";
        validator.addValue(key, "3");

        // When
        validator.expectMinValue(key, 5.0, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectMaxValueWithValidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "5");

        // When
        validator.expectMaxValue(key, 10.0);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectMaxValueWithInvalidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "15");

        // When
        validator.expectMaxValue(key, 10.0);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectMaxValueWithNonNumericValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "invalid");

        // When
        validator.expectMaxValue(key, 10.0);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectMaxValueWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectMaxValue(key, 10.0));
    }

    @Test
    void testExpectMaxValueWithCustomMessage() {
        // Given
        String key = "testKey";
        String customMessage = "Custom max value error";
        validator.addValue(key, "15");

        // When
        validator.expectMaxValue(key, 10.0, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectMinLengthWithValidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "hello");

        // When
        validator.expectMinLength(key, 3.0);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectMinLengthWithInvalidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "hi");

        // When
        validator.expectMinLength(key, 3.0);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectMinLengthWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectMinLength(key, 3.0));
    }

    @Test
    void testExpectMinLengthWithCustomMessage() {
        // Given
        String key = "testKey";
        String customMessage = "Custom min length error";
        validator.addValue(key, "hi");

        // When
        validator.expectMinLength(key, 3.0, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectMaxLengthWithValidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "hello");

        // When
        validator.expectMaxLength(key, 10.0);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectMaxLengthWithInvalidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "very long string");

        // When
        validator.expectMaxLength(key, 10.0);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectMaxLengthWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectMaxLength(key, 10.0));
    }

    @Test
    void testExpectMaxLengthWithCustomMessage() {
        // Given
        String key = "testKey";
        String customMessage = "Custom max length error";
        validator.addValue(key, "very long string");

        // When
        validator.expectMaxLength(key, 10.0, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectNumericWithValidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "123");

        // When
        validator.expectNumeric(key);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectNumericWithInvalidValue() {
        // Given
        String key = "testKey";
        validator.addValue(key, "abc");

        // When
        validator.expectNumeric(key);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectNumericWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectNumeric(key));
    }

    @Test
    void testExpectNumericWithCustomMessage() {
        // Given
        String key = "testKey";
        String customMessage = "Custom numeric error";
        validator.addValue(key, "abc");

        // When
        validator.expectNumeric(key, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectExactMatchWithMatchingValues() {
        // Given
        String key1 = "password";
        String key2 = "confirmPassword";
        validator.addValue(key1, "password123");
        validator.addValue(key2, "password123");

        // When
        validator.expectExactMatch(key1, key2);

        // Then
        assertFalse(validator.hasError(key1));
    }

    @Test
    void testExpectExactMatchWithNonMatchingValues() {
        // Given
        String key1 = "password";
        String key2 = "confirmPassword";
        validator.addValue(key1, "password123");
        validator.addValue(key2, "password456");

        // When
        validator.expectExactMatch(key1, key2);

        // Then
        assertTrue(validator.hasError(key1));
    }

    @Test
    void testExpectExactMatchWithNullKey() {
        // Given
        String key1 = null;
        String key2 = "confirmPassword";

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectExactMatch(key1, key2));
    }

    @Test
    void testExpectExactMatchWithCustomMessage() {
        // Given
        String key1 = "password";
        String key2 = "confirmPassword";
        String customMessage = "Custom exact match error";
        validator.addValue(key1, "password123");
        validator.addValue(key2, "password456");

        // When
        validator.expectExactMatch(key1, key2, customMessage);

        // Then
        assertTrue(validator.hasError(key1));
        assertEquals(customMessage, validator.getError(key1));
    }

    @Test
    void testExpectMatchWithMatchingValues() {
        // Given
        String key1 = "email";
        String key2 = "confirmEmail";
        validator.addValue(key1, "test@example.com");
        validator.addValue(key2, "test@example.com");

        // When
        validator.expectMatch(key1, key2);

        // Then
        assertFalse(validator.hasError(key1));
    }

    @Test
    void testExpectMatchWithCaseInsensitiveMatchingValues() {
        // Given
        String key1 = "email";
        String key2 = "confirmEmail";
        validator.addValue(key1, "Test@Example.com");
        validator.addValue(key2, "test@example.com");

        // When
        validator.expectMatch(key1, key2);

        // Then
        assertFalse(validator.hasError(key1));
    }

    @Test
    void testExpectMatchWithCustomMessage() {
        // Given
        String key1 = "email";
        String key2 = "confirmEmail";
        String customMessage = "Custom match error";
        validator.addValue(key1, "test@example.com");
        validator.addValue(key2, "different@example.com");

        // When
        validator.expectMatch(key1, key2, customMessage);

        // Then
        assertTrue(validator.hasError(key1));
        assertEquals(customMessage, validator.getError(key1));
    }

    @Test
    void testExpectMatchWithListOfValidValues() {
        // Given
        String key = "status";
        List<String> validValues = List.of("active", "inactive", "pending");
        validator.addValue(key, "active");

        // When
        validator.expectMatch(key, validValues);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectMatchWithListOfInvalidValues() {
        // Given
        String key = "status";
        List<String> validValues = List.of("active", "inactive", "pending");
        validator.addValue(key, "invalid");

        // When
        validator.expectMatch(key, validValues);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectMatchWithListAndCustomMessage() {
        // Given
        String key = "status";
        List<String> validValues = List.of("active", "inactive", "pending");
        String customMessage = "Custom list match error";
        validator.addValue(key, "invalid");

        // When
        validator.expectMatch(key, customMessage, validValues);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectEmailWithValidEmail() {
        // Given
        String key = "email";
        validator.addValue(key, "test@example.com");

        // When
        validator.expectEmail(key);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectEmailWithInvalidEmail() {
        // Given
        String key = "email";
        validator.addValue(key, "invalid-email");

        // When
        validator.expectEmail(key);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectEmailWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectEmail(key));
    }

    @Test
    void testExpectEmailWithCustomMessage() {
        // Given
        String key = "email";
        String customMessage = "Custom email error";
        validator.addValue(key, "invalid-email");

        // When
        validator.expectEmail(key, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectIpv4WithValidIpv4() {
        // Given
        String key = "ip";
        validator.addValue(key, "192.168.1.1");

        // When
        validator.expectIpv4(key);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectIpv4WithInvalidIpv4() {
        // Given
        String key = "ip";
        validator.addValue(key, "invalid-ip");

        // When
        validator.expectIpv4(key);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectIpv4WithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectIpv4(key));
    }

    @Test
    void testExpectIpv4WithCustomMessage() {
        // Given
        String key = "ip";
        String customMessage = "Custom IPv4 error";
        validator.addValue(key, "invalid-ip");

        // When
        validator.expectIpv4(key, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectIpv6WithValidIpv6() {
        // Given
        String key = "ip";
        validator.addValue(key, "2001:0db8:85a3:0000:0000:8a2e:0370:7334");

        // When
        validator.expectIpv6(key);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectIpv6WithInvalidIpv6() {
        // Given
        String key = "ip";
        validator.addValue(key, "invalid-ipv6");

        // When
        validator.expectIpv6(key);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectIpv6WithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectIpv6(key));
    }

    @Test
    void testExpectIpv6WithCustomMessage() {
        // Given
        String key = "ip";
        String customMessage = "Custom IPv6 error";
        validator.addValue(key, "invalid-ipv6");

        // When
        validator.expectIpv6(key, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectDomainNameWithValidDomain() {
        // Given
        String key = "domain";
        validator.addValue(key, "example.com");

        // When
        validator.expectDomainName(key);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectDomainNameWithInvalidDomain() {
        // Given
        String key = "domain";
        validator.addValue(key, "invalid-domain");

        // When
        validator.expectDomainName(key);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectDomainNameWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectDomainName(key));
    }

    @Test
    void testExpectDomainNameWithCustomMessage() {
        // Given
        String key = "domain";
        String customMessage = "Custom domain error";
        validator.addValue(key, "invalid-domain");

        // When
        validator.expectDomainName(key, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectUrlWithValidUrl() {
        // Given
        String key = "url";
        validator.addValue(key, "https://www.example.com");

        // When
        validator.expectUrl(key);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectUrlWithInvalidUrl() {
        // Given
        String key = "url";
        validator.addValue(key, "invalid-url");

        // When
        validator.expectUrl(key);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectUrlWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectUrl(key));
    }

    @Test
    void testExpectUrlWithCustomMessage() {
        // Given
        String key = "url";
        String customMessage = "Custom URL error";
        validator.addValue(key, "invalid-url");

        // When
        validator.expectUrl(key, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectRegexWithValidPattern() {
        // Given
        String key = "code";
        Pattern pattern = Pattern.compile("[A-Z]{3}");
        validator.addValue(key, "ABC");

        // When
        validator.expectRegex(key, pattern);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectRegexWithInvalidPattern() {
        // Given
        String key = "code";
        Pattern pattern = Pattern.compile("[A-Z]{3}");
        validator.addValue(key, "ab");

        // When
        validator.expectRegex(key, pattern);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectRegexWithNullKey() {
        // Given
        String key = null;
        Pattern pattern = Pattern.compile("[A-Z]{3}");

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectRegex(key, pattern));
    }

    @Test
    void testExpectRegexWithCustomMessage() {
        // Given
        String key = "code";
        Pattern pattern = Pattern.compile("[A-Z]{3}");
        String customMessage = "Custom regex error";
        validator.addValue(key, "ab");

        // When
        validator.expectRegex(key, pattern, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectTrueWithTrueValue() {
        // Given
        String key = "agreement";
        boolean value = true;

        // When
        validator.expectTrue(key, value);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectTrueWithFalseValue() {
        // Given
        String key = "agreement";
        boolean value = false;

        // When
        validator.expectTrue(key, value);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectTrueWithNullKey() {
        // Given
        String key = null;
        boolean value = true;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectTrue(key, value));
    }

    @Test
    void testExpectTrueWithCustomMessage() {
        // Given
        String key = "agreement";
        boolean value = false;
        String customMessage = "Custom true error";

        // When
        validator.expectTrue(key, value, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectFalseWithFalseValue() {
        // Given
        String key = "disabled";
        boolean value = false;

        // When
        validator.expectFalse(key, value);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectFalseWithTrueValue() {
        // Given
        String key = "disabled";
        boolean value = true;

        // When
        validator.expectFalse(key, value);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectFalseWithNullKey() {
        // Given
        String key = null;
        boolean value = false;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectFalse(key, value));
    }

    @Test
    void testExpectFalseWithCustomMessage() {
        // Given
        String key = "disabled";
        boolean value = true;
        String customMessage = "Custom false error";

        // When
        validator.expectFalse(key, value, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectNotNullWithNotNullValue() {
        // Given
        String key = "object";
        Object value = new Object();

        // When
        validator.expectNotNull(key, value);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectNotNullWithNullValue() {
        // Given
        String key = "object";
        Object value = null;

        // When
        validator.expectNotNull(key, value);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectNotNullWithNullKey() {
        // Given
        String key = null;
        Object value = new Object();

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectNotNull(key, value));
    }

    @Test
    void testExpectNotNullWithCustomMessage() {
        // Given
        String key = "object";
        Object value = null;
        String customMessage = "Custom not null error";

        // When
        validator.expectNotNull(key, value, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectNullWithNullValue() {
        // Given
        String key = "object";
        Object value = null;

        // When
        validator.expectNull(key, value);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectNullWithNotNullValue() {
        // Given
        String key = "object";
        Object value = new Object();

        // When
        validator.expectNull(key, value);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectNullWithNullKey() {
        // Given
        String key = null;
        Object value = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectNull(key, value));
    }

    @Test
    void testExpectNullWithCustomMessage() {
        // Given
        String key = "object";
        Object value = new Object();
        String customMessage = "Custom null error";

        // When
        validator.expectNull(key, value, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectRangeLengthWithValidLength() {
        // Given
        String key = "text";
        validator.addValue(key, "hello");

        // When
        validator.expectRangeLength(key, 3, 10);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectRangeLengthWithTooShortLength() {
        // Given
        String key = "text";
        validator.addValue(key, "hi");

        // When
        validator.expectRangeLength(key, 3, 10);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectRangeLengthWithTooLongLength() {
        // Given
        String key = "text";
        validator.addValue(key, "very long text");

        // When
        validator.expectRangeLength(key, 3, 10);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectRangeLengthWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectRangeLength(key, 3, 10));
    }

    @Test
    void testExpectRangeLengthWithCustomMessage() {
        // Given
        String key = "text";
        String customMessage = "Custom range length error";
        validator.addValue(key, "hi");

        // When
        validator.expectRangeLength(key, 3, 10, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectRangeValueWithValidValue() {
        // Given
        String key = "number";
        validator.addValue(key, "5");

        // When
        validator.expectRangeValue(key, 1, 10);

        // Then
        assertFalse(validator.hasError(key));
    }

    @Test
    void testExpectRangeValueWithTooSmallValue() {
        // Given
        String key = "number";
        validator.addValue(key, "0");

        // When
        validator.expectRangeValue(key, 1, 10);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectRangeValueWithTooLargeValue() {
        // Given
        String key = "number";
        validator.addValue(key, "15");

        // When
        validator.expectRangeValue(key, 1, 10);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectRangeValueWithNonNumericValue() {
        // Given
        String key = "number";
        validator.addValue(key, "invalid");

        // When
        validator.expectRangeValue(key, 1, 10);

        // Then
        assertTrue(validator.hasError(key));
    }

    @Test
    void testExpectRangeValueWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectRangeValue(key, 1, 10));
    }

    @Test
    void testExpectRangeValueWithCustomMessage() {
        // Given
        String key = "number";
        String customMessage = "Custom range value error";
        validator.addValue(key, "15");

        // When
        validator.expectRangeValue(key, 1, 10, customMessage);

        // Then
        assertTrue(validator.hasError(key));
        assertEquals(customMessage, validator.getError(key));
    }

    @Test
    void testExpectFileMaxSizeWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectFileMaxSize(key, 1000));
    }

    @Test
    void testExpectFileMimeTypeWithNullKey() {
        // Given
        String key = null;
        List<String> allowedMimeTypes = List.of("text/plain");

        // When & Then
        assertThrows(NullPointerException.class, () -> validator.expectFileMimeType(key, allowedMimeTypes));
    }
}
