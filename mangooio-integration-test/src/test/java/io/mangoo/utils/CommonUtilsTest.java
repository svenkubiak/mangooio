package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.crypto.Argon2Settings;
import org.apache.fory.Fory;
import org.apache.fory.config.Language;
import org.apache.fory.exception.InsecureException;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
@Execution(ExecutionMode.CONCURRENT)
class CommonUtilsTest {

    @BeforeAll
    static void registerSerializableTypes() {
        CommonUtils.registerSerializable(TestSerializableObject.class);
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
    void testHashArgon2NullCleartext() {
        //given
        String cleartext = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommonUtils.hashArgon2(cleartext));
        assertThat(exception.getMessage(), containsString("cleartext can not be null"));
    }

    @Test
    void testHashArgon2IsPhcFormattedWithTheConfiguredParameters() {
        //given
        String cleartext = "test-password";
        String salt = "test-salt";
        var config = Application.getInstance(Config.class);

        //when
        String hash = CommonUtils.hashArgon2(cleartext, salt);

        //then
        assertThat(hash, startsWith("$argon2id$v=19$m=%s,t=%s,p=%s$".formatted(
                config.getAuthenticationHashingMemory(),
                config.getAuthenticationHashingIterations(),
                config.getAuthenticationHashingParallelism())));
        assertThat(hash.split("\\$").length, equalTo(6));
    }

    @Test
    void testFreshHashDoesNotNeedRehash() {
        //given
        String hash = CommonUtils.hashArgon2("test-password", "test-salt");

        //then
        assertThat(CommonUtils.needsRehash(hash), equalTo(false));
    }

    @Test
    void testLegacyHashStillMatchesAndNeedsRehash() {
        //given a bare Base64 hash as earlier mangoo I/O versions stored it
        String cleartext = "test-password";
        String salt = "test-salt";
        String legacy = legacyHash(cleartext, salt);

        //then
        assertThat(CommonUtils.matchArgon2(cleartext, salt, legacy), equalTo(true));
        assertThat(CommonUtils.matchArgon2("wrong-password", salt, legacy), equalTo(false));
        assertThat(CommonUtils.needsRehash(legacy), equalTo(true));
    }

    private static String legacyHash(String cleartext, String salt) {
        return Base64.getEncoder().encodeToString(argon2(cleartext, salt, Argon2Settings.LEGACY));
    }

    private static byte[] argon2(String cleartext, String salt, Argon2Settings settings) {
        var parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withParallelism(settings.parallelism())
                .withMemoryAsKB(settings.memoryKb())
                .withSalt(salt.getBytes(StandardCharsets.UTF_8))
                .withIterations(settings.iterations())
                .build();

        var generator = new Argon2BytesGenerator();
        generator.init(parameters);

        var hash = new byte[32];
        generator.generateBytes(cleartext.getBytes(StandardCharsets.UTF_8), hash);

        return hash;
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
    void testMatchArgon2WithSaltWrongSaltForALegacyHash() {
        //given a legacy hash has no salt of its own, the given one is used
        String cleartext = "test-password";
        String salt = "test-salt";
        String wrongSalt = "wrong-salt";
        String hash = legacyHash(cleartext, salt);

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

        //then SHA3-512 of the UTF-8 bytes as lowercase hex
        assertThat(hash, equalTo("5196a85e2963e0006fde0f5e289172239e7ba456a311a5066a4a4335e5dabfb0"
                + "8faac91ca3ba50a0b9605ee07b39558303ec7a976a3157d6bfd66ecd25c755e9"));
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
    void testDeserializeFromBase64DoesNotInstantiateUnregisteredClass() {
        //given
        Fory fory = Fory.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(false)
                .build();
        String data = Base64.getEncoder().encodeToString(fory.serialize(new UnregisteredObject("payload")));

        //when
        Object deserialized = CommonUtils.deserializeFromBase64(data);

        //then
        assertThat(deserialized, not(instanceOf(UnregisteredObject.class)));
    }

    @Test
    void testSerializeToBase64RejectsUnregisteredClass() {
        //when & then
        assertThrows(InsecureException.class, () -> CommonUtils.serializeToBase64(new UnregisteredObject("payload")));
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
        assertThat(new String(encoded, StandardCharsets.UTF_8), equalTo("dGVzdC1kYXRh"));
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
        assertThat(encoded, equalTo("ORSXG5BNMRQXIYI="));
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
        byte[] data = "test-data".getBytes(StandardCharsets.UTF_8);

        //when
        byte[] encoded = CommonUtils.encodeToBase64(data);

        //then
        assertThat(new String(encoded, StandardCharsets.UTF_8), equalTo("dGVzdC1kYXRh"));
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
        assertThat(UUID.fromString(uuid).version(), equalTo(6));
    }

    @Test
    void testUuidV7() {
        //when
        String uuid = CommonUtils.uuidV7();

        //then
        assertThat(UUID.fromString(uuid).version(), equalTo(7));
    }

    @Test
    void testUuidV4() {
        //when
        String uuid = CommonUtils.uuidV4();

        //then
        assertThat(UUID.fromString(uuid).version(), equalTo(4));
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

        //when
        Map<String, String> copiedMap = CommonUtils.copyMap(originalMap);
        copiedMap.put("key2", "value2");

        //then
        assertThat(copiedMap.get("key1"), equalTo("value1"));
        assertThat(originalMap.containsKey("key2"), equalTo(false));
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
        assertThat(randomString.length(), equalTo(length));
    }

    @Test
    void testRandomStringMinimumLength() {
        //given
        int length = 22; // MIN_LENGTH

        //when
        String randomString = CommonUtils.randomString(length);

        //then
        assertThat(randomString.length(), equalTo(length));
    }

    @Test
    void testRandomStringMaximumLength() {
        //given
        int length = 512; // MAX_LENGTH

        //when
        String randomString = CommonUtils.randomString(length);

        //then
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

    private record UnregisteredObject(String value) implements Serializable {
    }

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