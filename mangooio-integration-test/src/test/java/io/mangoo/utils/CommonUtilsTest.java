package io.mangoo.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class CommonUtilsTest {

    @Test
    void testHashArgon2WithSalt() {
        //given
        String cleartext = "test-password";
        String salt = "test-salt";

        //when
        String hash = CommonUtils.hashArgon2(cleartext, salt);

        //then
        assertThat(hash, not(nullValue()));
        assertThat(hash, not(emptyString()));
        assertThat(hash.length(), greaterThan(0));
    }

    @Test
    void testHashArgon2WithSaltNullCleartext() {
        //given
        String cleartext = null;
        String salt = "test-salt";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.hashArgon2(cleartext, salt));
        assertThat(exception.getMessage(), containsString("cleartext can not be null"));
    }

    @Test
    void testHashArgon2WithSaltNullSalt() {
        //given
        String cleartext = "test-password";
        String salt = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.hashArgon2(cleartext, salt));
        assertThat(exception.getMessage(), containsString("salt can not be null"));
    }

    @Test
    void testHashArgon2WithSaltConsistency() {
        //given
        String cleartext = "test-password";
        String salt = "test-salt";

        //when
        String hash1 = CommonUtils.hashArgon2(cleartext, salt);
        String hash2 = CommonUtils.hashArgon2(cleartext, salt);

        //then
        assertThat(hash1, equalTo(hash2));
    }

    @Test
    void testHashArgon2WithSaltDifferentInputs() {
        //given
        String cleartext1 = "test-password";
        String cleartext2 = "different-password";
        String salt = "test-salt";

        //when
        String hash1 = CommonUtils.hashArgon2(cleartext1, salt);
        String hash2 = CommonUtils.hashArgon2(cleartext2, salt);

        //then
        assertThat(hash1, not(equalTo(hash2)));
    }

    @Test
    void testHashArgon2WithSaltDifferentSalts() {
        //given
        String cleartext = "test-password";
        String salt1 = "salt1";
        String salt2 = "salt2";

        //when
        String hash1 = CommonUtils.hashArgon2(cleartext, salt1);
        String hash2 = CommonUtils.hashArgon2(cleartext, salt2);

        //then
        assertThat(hash1, not(equalTo(hash2)));
    }

    @Test
    void testHashArgon2NullCleartext() {
        //given
        String cleartext = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.hashArgon2(cleartext));
        assertThat(exception.getMessage(), containsString("cleartext can not be null"));
    }

    @Test
    void testMatchArgon2WithSalt() {
        //given
        String cleartext = "test-password";
        String salt = "test-salt";
        String hash = CommonUtils.hashArgon2(cleartext, salt);

        //when
        boolean match = CommonUtils.matchArgon2(cleartext, salt, hash);

        //then
        assertThat(match, equalTo(true));
    }

    @Test
    void testMatchArgon2WithSaltWrongPassword() {
        //given
        String cleartext = "test-password";
        String wrongCleartext = "wrong-password";
        String salt = "test-salt";
        String hash = CommonUtils.hashArgon2(cleartext, salt);

        //when
        boolean match = CommonUtils.matchArgon2(wrongCleartext, salt, hash);

        //then
        assertThat(match, equalTo(false));
    }

    @Test
    void testMatchArgon2WithSaltWrongSalt() {
        //given
        String cleartext = "test-password";
        String salt = "test-salt";
        String wrongSalt = "wrong-salt";
        String hash = CommonUtils.hashArgon2(cleartext, salt);

        //when
        boolean match = CommonUtils.matchArgon2(cleartext, wrongSalt, hash);

        //then
        assertThat(match, equalTo(false));
    }

    @Test
    void testMatchArgon2WithSaltNullCleartext() {
        //given
        String cleartext = null;
        String salt = "test-salt";
        String hash = "test-hash";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.matchArgon2(cleartext, salt, hash));
        assertThat(exception.getMessage(), containsString("cleartext can not be null"));
    }

    @Test
    void testMatchArgon2WithSaltNullSalt() {
        //given
        String cleartext = "test-password";
        String salt = null;
        String hash = "test-hash";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.matchArgon2(cleartext, salt, hash));
        assertThat(exception.getMessage(), containsString("salt can not be null"));
    }

    @Test
    void testMatchArgon2WithSaltNullHash() {
        //given
        String cleartext = "test-password";
        String salt = "test-salt";
        String hash = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.matchArgon2(cleartext, salt, hash));
        assertThat(exception.getMessage(), containsString("hash can not be null"));
    }

    @Test
    void testMatchArgon2NullCleartext() {
        //given
        String cleartext = null;
        String hash = "test-hash";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.matchArgon2(cleartext, hash));
        assertThat(exception.getMessage(), containsString("cleartext can not be null"));
    }

    @Test
    void testMatchArgon2NullHash() {
        //given
        String cleartext = "test-password";
        String hash = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.matchArgon2(cleartext, hash));
        assertThat(exception.getMessage(), containsString("hash can not be null"));
    }

    @Test
    void testHexSHA512() {
        //given
        String data = "test-data";

        //when
        String hash = CommonUtils.hexSHA512(data);

        //then
        assertThat(hash, not(nullValue()));
        assertThat(hash, not(emptyString()));
        assertThat(hash.length(), equalTo(128)); // SHA3-512 produces 128 hex characters
    }

    @Test
    void testHexSHA512NullData() {
        //given
        String data = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.hexSHA512(data));
        assertThat(exception.getMessage(), containsString("data can not be null"));
    }

    @Test
    void testHexSHA512Consistency() {
        //given
        String data = "test-data";

        //when
        String hash1 = CommonUtils.hexSHA512(data);
        String hash2 = CommonUtils.hexSHA512(data);

        //then
        assertThat(hash1, equalTo(hash2));
    }

    @Test
    void testHexSHA512DifferentInputs() {
        //given
        String data1 = "test-data1";
        String data2 = "test-data2";

        //when
        String hash1 = CommonUtils.hexSHA512(data1);
        String hash2 = CommonUtils.hexSHA512(data2);

        //then
        assertThat(hash1, not(equalTo(hash2)));
    }

    @Test
    void testSerializeToBase64() {
        //given
        TestSerializableObject object = new TestSerializableObject("test-value", 123);

        //when
        String serialized = CommonUtils.serializeToBase64(object);

        //then
        assertThat(serialized, not(nullValue()));
        assertThat(serialized, not(emptyString()));
    }

    @Test
    void testSerializeToBase64NullObject() {
        //given
        Serializable object = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> CommonUtils.serializeToBase64(object));
        assertThat(exception.getMessage(), containsString("object can not be null"));
    }

    @Test
    void testDeserializeFromBase64() {
        //given
        TestSerializableObject originalObject = new TestSerializableObject("test-value", 123);
        String serialized = CommonUtils.serializeToBase64(originalObject);

        //when
        TestSerializableObject deserializedObject = CommonUtils.deserializeFromBase64(serialized);

        //then
        assertThat(deserializedObject, not(nullValue()));
        assertThat(deserializedObject.getValue(), equalTo(originalObject.getValue()));
        assertThat(deserializedObject.getNumber(), equalTo(originalObject.getNumber()));
    }

    @Test
    void testDeserializeFromBase64NullData() {
        //given
        String data = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.deserializeFromBase64(data));
        assertThat(exception.getMessage(), containsString("data can not be null"));
    }

    @Test
    void testEncodeToBase64String() {
        //given
        String data = "test-data";

        //when
        byte[] encoded = CommonUtils.encodeToBase64(data);

        //then
        assertThat(encoded, not(nullValue()));
        assertThat(encoded.length, greaterThan(0));
    }

    @Test
    void testEncodeToBase64StringNullData() {
        //given
        String data = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.encodeToBase64(data));
        assertThat(exception.getMessage(), containsString("data can not be null"));
    }

    @Test
    void testEncodeToBase32() {
        //given
        String data = "test-data";

        //when
        String encoded = CommonUtils.encodeToBase32(data);

        //then
        assertThat(encoded, not(nullValue()));
        assertThat(encoded, not(emptyString()));
    }

    @Test
    void testEncodeToBase32NullData() {
        //given
        String data = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.encodeToBase32(data));
        assertThat(exception.getMessage(), containsString("data can not be null"));
    }

    @Test
    void testEncodeToBase64ByteArray() {
        //given
        byte[] data = "test-data".getBytes();

        //when
        byte[] encoded = CommonUtils.encodeToBase64(data);

        //then
        assertThat(encoded, not(nullValue()));
        assertThat(encoded.length, greaterThan(0));
    }

    @Test
    void testEncodeToBase64ByteArrayNullData() {
        //given
        byte[] data = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> CommonUtils.encodeToBase64(data));
        assertThat(exception.getMessage(), containsString("data can not be null"));
    }

    @Test
    void testDecodeFromBase64() {
        //given
        String originalData = "test-data";
        byte[] encoded = CommonUtils.encodeToBase64(originalData);
        String encodedString = new String(encoded);

        //when
        byte[] decoded = CommonUtils.decodeFromBase64(encodedString);

        //then
        assertThat(decoded, not(nullValue()));
        assertThat(new String(decoded), equalTo(originalData));
    }

    @Test
    void testDecodeFromBase64NullData() {
        //given
        String data = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.decodeFromBase64(data));
        assertThat(exception.getMessage(), containsString("data can not be null"));
    }

    @Test
    void testUuidV6() {
        //when
        String uuid = CommonUtils.uuidV6();

        //then
        assertThat(uuid, not(nullValue()));
        assertThat(uuid, not(emptyString()));
        assertThat(uuid.length(), equalTo(36)); // Standard UUID length
        assertThat(uuid, matchesRegex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    void testUuidV6Uniqueness() {
        //when
        String uuid1 = CommonUtils.uuidV6();
        String uuid2 = CommonUtils.uuidV6();

        //then
        assertThat(uuid1, not(equalTo(uuid2)));
    }

    @Test
    void testUuidV4() {
        //when
        String uuid = CommonUtils.uuidV4();

        //then
        assertThat(uuid, not(nullValue()));
        assertThat(uuid, not(emptyString()));
        assertThat(uuid.length(), equalTo(36)); // Standard UUID length
        assertThat(uuid, matchesRegex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    void testUuidV4Uniqueness() {
        //when
        String uuid1 = CommonUtils.uuidV4();
        String uuid2 = CommonUtils.uuidV4();

        //then
        assertThat(uuid1, not(equalTo(uuid2)));
    }

    @Test
    void testBitLengthByteArray() {
        //given
        byte[] bytes = "test".getBytes();

        //when
        int bitLength = CommonUtils.bitLength(bytes);

        //then
        assertThat(bitLength, equalTo(bytes.length * 8));
    }

    @Test
    void testBitLengthByteArrayNull() {
        //given
        byte[] bytes = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> CommonUtils.bitLength(bytes));
        assertThat(exception.getMessage(), containsString("bytes can not be null"));
    }

    @Test
    void testBitLengthByteArrayEmpty() {
        //given
        byte[] bytes = new byte[0];

        //when
        int bitLength = CommonUtils.bitLength(bytes);

        //then
        assertThat(bitLength, equalTo(0));
    }

    @Test
    void testBitLengthByteArrayLarge() {
        //given
        byte[] bytes = new byte[Integer.MAX_VALUE / 8 + 1];

        //when
        int bitLength = CommonUtils.bitLength(bytes);

        //then
        assertThat(bitLength, equalTo(0));
    }

    @Test
    void testBitLengthString() {
        //given
        String string = "test";

        //when
        int bitLength = CommonUtils.bitLength(string);

        //then
        assertThat(bitLength, equalTo(string.getBytes().length * 8));
    }

    @Test
    void testBitLengthStringNull() {
        //given
        String string = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.bitLength(string));
        assertThat(exception.getMessage(), containsString("string can not be null"));
    }

    @Test
    void testCopyMap() {
        //given
        Map<String, String> originalMap = new HashMap<>();
        originalMap.put("key1", "value1");
        originalMap.put("key2", "value2");

        //when
        Map<String, String> copiedMap = CommonUtils.copyMap(originalMap);

        //then
        assertThat(copiedMap, not(sameInstance(originalMap)));
        assertThat(copiedMap, equalTo(originalMap));
        assertThat(copiedMap.size(), equalTo(originalMap.size()));
    }

    @Test
    void testCopyMapNull() {
        //given
        Map<String, String> originalMap = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> CommonUtils.copyMap(originalMap));
        assertThat(exception.getMessage(), containsString("map can not be null"));
    }

    @Test
    void testCopyMapEmpty() {
        //given
        Map<String, String> originalMap = new HashMap<>();

        //when
        Map<String, String> copiedMap = CommonUtils.copyMap(originalMap);

        //then
        assertThat(copiedMap, not(sameInstance(originalMap)));
        assertThat(copiedMap, equalTo(originalMap));
        assertThat(copiedMap.size(), equalTo(0));
    }

    @Test
    void testToStringMap() {
        //given
        Map<String, Object> originalMap = new HashMap<>();
        originalMap.put("string", "value");
        originalMap.put("integer", 123);
        originalMap.put("null", null);

        //when
        Map<String, String> stringMap = CommonUtils.toStringMap(originalMap);

        //then
        assertThat(stringMap, not(sameInstance(originalMap)));
        assertThat(stringMap.get("string"), equalTo("value"));
        assertThat(stringMap.get("integer"), equalTo("123"));
        assertThat(stringMap.get("null"), nullValue());
    }

    @Test
    void testToStringMapNull() {
        //given
        Map<String, Object> originalMap = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> CommonUtils.toStringMap(originalMap));
        assertThat(exception.getMessage(), containsString("map can not be null"));
    }

    @Test
    void testToStringMapEmpty() {
        //given
        Map<String, Object> originalMap = new HashMap<>();

        //when
        Map<String, String> stringMap = CommonUtils.toStringMap(originalMap);

        //then
        assertThat(stringMap, not(sameInstance(originalMap)));
        assertThat(stringMap.size(), equalTo(0));
    }

    @Test
    void testRandomString() {
        //given
        int length = 30;

        //when
        String randomString = CommonUtils.randomString(length);

        //then
        assertThat(randomString, not(nullValue()));
        assertThat(randomString.length(), equalTo(length));
    }

    @Test
    void testRandomStringMinimumLength() {
        //given
        int length = 22; // MIN_LENGTH

        //when
        String randomString = CommonUtils.randomString(length);

        //then
        assertThat(randomString, not(nullValue()));
        assertThat(randomString.length(), equalTo(length));
    }

    @Test
    void testRandomStringMaximumLength() {
        //given
        int length = 512; // MAX_LENGTH

        //when
        String randomString = CommonUtils.randomString(length);

        //then
        assertThat(randomString, not(nullValue()));
        assertThat(randomString.length(), equalTo(length));
    }

    @Test
    void testRandomStringTooShort() {
        //given
        int length = 21; // Less than MIN_LENGTH

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.randomString(length));
        assertThat(exception.getMessage(), containsString("Length must be at least 22 characters for security"));
    }

    @Test
    void testRandomStringTooLong() {
        //given
        int length = 513; // Greater than MAX_LENGTH

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.randomString(length));
        assertThat(exception.getMessage(), containsString("Length must not exceed 512 characters"));
    }

    @Test
    void testRandomStringUniqueness() {
        //given
        int length = 30;

        //when
        String randomString1 = CommonUtils.randomString(length);
        String randomString2 = CommonUtils.randomString(length);

        //then
        assertThat(randomString1, not(equalTo(randomString2)));
    }

    @Test
    void testResourceExists() {
        //given
        String resourceName = "version.properties"; // Known to exist in the project

        //when
        boolean exists = CommonUtils.resourceExists(resourceName);

        //then
        assertThat(exists, equalTo(true));
    }

    @Test
    void testResourceExistsNonExistent() {
        //given
        String resourceName = "non-existent-resource.txt";

        //when
        boolean exists = CommonUtils.resourceExists(resourceName);

        //then
        assertThat(exists, equalTo(false));
    }

    @Test
    void testResourceExistsNull() {
        //given
        String resourceName = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.resourceExists(resourceName));
        assertThat(exception.getMessage(), containsString("name can not be null"));
    }

    @Test
    void testReadResourceToString() {
        //given
        String resource = "version.properties"; // Known to exist in the project

        //when
        String content = CommonUtils.readResourceToString(resource);

        //then
        assertThat(content, not(nullValue()));
        assertThat(content, not(emptyString()));
    }

    @Test
    void testReadResourceToStringNonExistent() {
        //given
        String resource = "non-existent-resource.txt";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.readResourceToString(resource));
        assertThat(exception.getMessage(), containsString("resource non-existent-resource.txt not found."));
    }

    @Test
    void testReadResourceToStringNull() {
        //given
        String resource = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.readResourceToString(resource));
        assertThat(exception.getMessage(), containsString("resource can not be null"));
    }

    // Helper class for serialization tests
    private static class TestSerializableObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String value;
        private final int number;

        public TestSerializableObject(String value, int number) {
            this.value = value;
            this.number = number;
        }

        public String getValue() {
            return value;
        }

        public int getNumber() {
            return number;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestSerializableObject that = (TestSerializableObject) o;
            return number == that.number && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, number);
        }
    }
}