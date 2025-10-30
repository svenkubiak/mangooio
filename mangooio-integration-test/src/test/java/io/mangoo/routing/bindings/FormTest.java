package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({TestExtension.class})
class FormTest {

    private Form form;

    @BeforeEach
    void setUp() {
        form = new Form();
    }

    @Test
    void testGetStringWithValidValue() {
        // Given
        String key = "testKey";
        String expectedValue = "testValue";
        form.addValue(key, expectedValue);

        // When
        Optional<String> result = form.getString(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedValue, result.get());
    }

    @Test
    void testGetStringWithBlankValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "   ");

        // When
        Optional<String> result = form.getString(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStringWithEmptyValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "");

        // When
        Optional<String> result = form.getString(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStringWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> form.getString(key));
    }

    @Test
    void testGetStringWithNonExistentKey() {
        // Given
        String key = "nonExistentKey";

        // When
        Optional<String> result = form.getString(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetValueWithValidValue() {
        // Given
        String key = "testKey";
        String expectedValue = "testValue";
        form.addValue(key, expectedValue);

        // When
        String result = form.getValue(key);

        // Then
        assertEquals(expectedValue, result);
    }

    @Test
    void testGetValueWithBlankValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "   ");

        // When
        String result = form.getValue(key);

        // Then
        assertEquals("", result);
    }

    @Test
    void testGetValueWithEmptyValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "");

        // When
        String result = form.getValue(key);

        // Then
        assertEquals("", result);
    }

    @Test
    void testGetValueWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> form.getValue(key));
    }

    @Test
    void testGetValueWithNonExistentKey() {
        // Given
        String key = "nonExistentKey";

        // When
        String result = form.getValue(key);

        // Then
        assertEquals("", result);
    }

    @Test
    void testGetBooleanWithTrueValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "true");

        // When
        Optional<Boolean> result = form.getBoolean(key);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void testGetBooleanWithOneValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "1");

        // When
        Optional<Boolean> result = form.getBoolean(key);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void testGetBooleanWithFalseValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "false");

        // When
        Optional<Boolean> result = form.getBoolean(key);

        // Then
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    void testGetBooleanWithZeroValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "0");

        // When
        Optional<Boolean> result = form.getBoolean(key);

        // Then
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    void testGetBooleanWithInvalidValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "invalid");

        // When
        Optional<Boolean> result = form.getBoolean(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetBooleanWithBlankValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "   ");

        // When
        Optional<Boolean> result = form.getBoolean(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetBooleanWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> form.getBoolean(key));
    }

    @Test
    void testGetIntegerWithValidValue() {
        // Given
        String key = "testKey";
        String value = "42";
        form.addValue(key, value);

        // When
        Optional<Integer> result = form.getInteger(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testGetIntegerWithNegativeValue() {
        // Given
        String key = "testKey";
        String value = "-42";
        form.addValue(key, value);

        // When
        Optional<Integer> result = form.getInteger(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(-42, result.get());
    }

    @Test
    void testGetIntegerWithInvalidValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "invalid");

        // When
        Optional<Integer> result = form.getInteger(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetIntegerWithBlankValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "   ");

        // When
        Optional<Integer> result = form.getInteger(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetIntegerWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> form.getInteger(key));
    }

    @Test
    void testGetDoubleWithValidValue() {
        // Given
        String key = "testKey";
        String value = "42.5";
        form.addValue(key, value);

        // When
        Optional<Double> result = form.getDouble(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(42.5, result.get());
    }

    @Test
    void testGetDoubleWithNegativeValue() {
        // Given
        String key = "testKey";
        String value = "-42.5";
        form.addValue(key, value);

        // When
        Optional<Double> result = form.getDouble(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(-42.5, result.get());
    }

    @Test
    void testGetDoubleWithIntegerValue() {
        // Given
        String key = "testKey";
        String value = "42";
        form.addValue(key, value);

        // When
        Optional<Double> result = form.getDouble(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(42.0, result.get());
    }

    @Test
    void testGetDoubleWithInvalidValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "invalid");

        // When
        Optional<Double> result = form.getDouble(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetDoubleWithBlankValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "   ");

        // When
        Optional<Double> result = form.getDouble(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetDoubleWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> form.getDouble(key));
    }

    @Test
    void testGetFloatWithValidValue() {
        // Given
        String key = "testKey";
        String value = "42.5f";
        form.addValue(key, value);

        // When
        Optional<Float> result = form.getFloat(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(42.5f, result.get());
    }

    @Test
    void testGetFloatWithNegativeValue() {
        // Given
        String key = "testKey";
        String value = "-42.5";
        form.addValue(key, value);

        // When
        Optional<Float> result = form.getFloat(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(-42.5f, result.get());
    }

    @Test
    void testGetFloatWithIntegerValue() {
        // Given
        String key = "testKey";
        String value = "42";
        form.addValue(key, value);

        // When
        Optional<Float> result = form.getFloat(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(42.0f, result.get());
    }

    @Test
    void testGetFloatWithInvalidValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "invalid");

        // When
        Optional<Float> result = form.getFloat(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFloatWithBlankValue() {
        // Given
        String key = "testKey";
        form.addValue(key, "   ");

        // When
        Optional<Float> result = form.getFloat(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFloatWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> form.getFloat(key));
    }

    @Test
    void testGetFileWithExistingFile() {
        // Given
        String key = "testFile";
        byte[] fileContent = "test content".getBytes();
        form.addFile(key, new ByteArrayInputStream(fileContent));

        // When
        Optional<byte[]> result = form.getFile(key);

        // Then
        assertTrue(result.isPresent());
        assertArrayEquals(fileContent, result.get());
    }

    @Test
    void testGetFileWithNonExistentFile() {
        // Given
        String key = "nonExistentFile";

        // When
        Optional<byte[]> result = form.getFile(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFileWithEmptyFilesMap() {
        // Given
        String key = "testFile";

        // When
        Optional<byte[]> result = form.getFile(key);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFileWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> form.getFile(key));
    }

    @Test
    void testGetValues() {
        // Given
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("key1", "value1");
        expectedValues.put("key2", "value2");
        form.setValues(expectedValues);

        // When
        Map<String, String> result = form.getValues();

        // Then
        assertEquals(expectedValues, result);
    }

    @Test
    void testAddFileWithValidInputStream() {
        // Given
        String key = "testFile";
        String content = "test file content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        // When
        form.addFile(key, inputStream);

        // Then
        Optional<byte[]> result = form.getFile(key);
        assertTrue(result.isPresent());
        assertArrayEquals(content.getBytes(), result.get());
    }

    @Test
    void testAddFileWithNullKey() {
        // Given
        String key = null;
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        // When & Then
        assertThrows(NullPointerException.class, () -> form.addFile(key, inputStream));
    }

    @Test
    void testAddFileWithNullInputStream() {
        // Given
        String key = "testFile";
        InputStream inputStream = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> form.addFile(key, inputStream));
    }

    @Test
    void testAddFileWithIOException() {
        // Given
        String key = "testFile";
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Test IOException");
            }
        };

        // When
        form.addFile(key, inputStream);

        // Then
        // Should not throw exception, but file should not be added
        Optional<byte[]> result = form.getFile(key);
        assertTrue(result.isEmpty());
    }

    @Test
    void testKeep() {
        // Given
        assertFalse(form.isKept());

        // When
        form.keep();

        // Then
        assertTrue(form.isKept());
    }

    @Test
    void testIsKeptInitiallyFalse() {
        // When
        boolean result = form.isKept();

        // Then
        assertFalse(result);
    }

    @Test
    void testDiscard() {
        // Given
        form.addValue("key1", "value1");
        form.addValue("key2", "value2");
        form.addFile("file1", new ByteArrayInputStream("content1".getBytes()));
        form.addFile("file2", new ByteArrayInputStream("content2".getBytes()));

        // When
        form.discard();

        // Then
        assertTrue(form.getValues().isEmpty());
        assertTrue(form.getFile("file1").isEmpty());
        assertTrue(form.getFile("file2").isEmpty());
    }

    @Test
    void testIsSubmittedInitiallyFalse() {
        // When
        boolean result = form.isSubmitted();

        // Then
        assertFalse(result);
    }

    @Test
    void testSetSubmitted() {
        // Given
        assertFalse(form.isSubmitted());

        // When
        form.setSubmitted(true);

        // Then
        assertTrue(form.isSubmitted());
    }

    @Test
    void testSetSubmittedFalse() {
        // Given
        form.setSubmitted(true);
        assertTrue(form.isSubmitted());

        // When
        form.setSubmitted(false);

        // Then
        assertFalse(form.isSubmitted());
    }
}
