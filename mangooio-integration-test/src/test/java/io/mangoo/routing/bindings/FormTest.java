package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import io.mangoo.utils.internal.MangooUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    void testGetFileWithExistingFile() throws IOException {
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
    void testGetFileWithNullKey() {
        // Given
        String key = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> form.getFile(key));
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
    void testAddFileWithIOException() throws IOException {
        // Given
        String key = "testFile";
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Test IOException");
            }
        };

        // Then
        assertThrows(IOException.class, () -> form.addFile(key, inputStream));

        // Then
        Optional<byte[]> result = form.getFile(key);
        assertTrue(result.isEmpty());
    }



    @Test
    void testDiscard() throws IOException {
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
    void testFlashSerializationExcludesFiles() throws IOException {
        // Given
        form.addValue("email", "foo@bar.com");
        form.addFile("upload", new ByteArrayInputStream(new byte[64 * 1024]));

        // When
        String serialized = MangooUtils.serializeFlashFormToBase64(form);
        Form deserialized = MangooUtils.deserializeFlashFormFromBase64(serialized);

        // Then
        // A 64 KiB upload would alone add roughly 87k characters of Base64, so anything
        // in this range proves the file bytes are not part of the serialization
        assertTrue(serialized.length() < 4096,
                "Serialized flash form must not contain the uploaded file bytes, but was " + serialized.length() + " characters");
        assertEquals("foo@bar.com", deserialized.getValue("email"));
        assertTrue(deserialized.getFile("upload").isEmpty());
    }


    @Test
    void testGetLongWithValuesLongCannotParse() {
        // Given
        form.addValue("big", "99999999999");
        form.addValue("decimal", "1.5");
        form.addValue("hex", "0x1F");

        // Then
        assertEquals(Optional.of(99999999999L), form.getLong("big"));
        assertTrue(form.getLong("decimal").isEmpty());
        assertTrue(form.getLong("hex").isEmpty());
    }

    @Test
    void testGetDoubleAndFloatWithValuesTheyCannotParse() {
        // Given
        form.addValue("decimal", "1.5");

        // Then
        assertEquals(Optional.of(1.5), form.getDouble("decimal"));
        assertEquals(Optional.of(1.5f), form.getFloat("decimal"));

        for (String value : new String[] {"0x1F", "1L", "NaN", "Infinity", "-Infinity"}) {
            // Given
            form.addValue("number", value);

            // Then
            assertTrue(form.getDouble("number").isEmpty(), "Expected empty double for " + value);
            assertTrue(form.getFloat("number").isEmpty(), "Expected empty float for " + value);
        }
    }
}
