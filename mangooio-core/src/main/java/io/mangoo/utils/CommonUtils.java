package io.mangoo.utils;

import com.fasterxml.uuid.Generators;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.Const;
import io.mangoo.constants.Key;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import org.apache.commons.codec.binary.Base32;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public final class CommonUtils {
    private static final Logger LOG = LogManager.getLogger(CommonUtils.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MAX_LENGTH = 512;
    private static final int MIN_LENGTH = 22;
    private static final Base32 BASE32ENCODER = new Base32();
    private static final Base64.Encoder BASE64ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64DECODER = Base64.getDecoder();
    private static final int BYTES = 8;
    private static final int MAX_BYTE_LENGTH = Integer.MAX_VALUE / 8;
    private static final ThreadSafeFury FURY = Fury.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(false)
            .buildThreadSafeFury();

    private CommonUtils() {
    }
    
    /**
     * Hashes a given clear text  with a given salt using Argon2Id hashing
     * 
     * @param cleartext The clear text
     * @param salt The salt
     * 
     * @return A Base64 encoded String
     */
    public static String hashArgon2(String cleartext, String salt) {
        Arguments.requireNonBlank(cleartext, Required.CLEARTEXT);
        Arguments.requireNonBlank(salt, Required.SALT);

        var argon2 = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withParallelism(2)
                .withMemoryAsKB(80000)
                .withSalt(salt.getBytes(StandardCharsets.UTF_8))
                .withIterations(6)
                .build();

        var argon2Generator = new Argon2BytesGenerator();
        argon2Generator.init(argon2);

        var hash = new byte[32];
        argon2Generator.generateBytes(cleartext.getBytes(StandardCharsets.UTF_8), hash);

        return BASE64ENCODER.encodeToString(hash);
    }

    /**
     * Hashes a given clear text with the application secret as salt using Argon2Id hashing
     *
     * @param cleartext The clear text
     *
     * @return A Base64 encoded String
     */
    public static String hashArgon2(String cleartext) {
        Arguments.requireNonBlank(cleartext, Required.CLEARTEXT);

        var salt = Application.getInstance(Config.class).getString(Key.APPLICATION_SECRET);
        return hashArgon2(cleartext, salt);
    }
    
    /**
     * Matches a given clear text with salt using Argon2Id against an already Argon2Id hashed value
     * 
     * @param cleartext The clear text
     * @param salt The salt
     * @param hash The hashed value for comparison (must be Base64 encoded)
     * 
     * @return True if hashes match, false otherwise
     */
    public static boolean matchArgon2(String cleartext, String salt, String hash) {
        Arguments.requireNonBlank(cleartext, Required.CLEARTEXT);
        Arguments.requireNonBlank(salt, Required.SALT);
        Arguments.requireNonBlank(hash, Required.HASH);

        return Arrays.areEqual(hashArgon2(cleartext, salt).getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Matches a given clear text using the application secret as salt using Argon2Id against an already Argon2Id hashed value
     *
     * @param cleartext The clear text
     * @param hash The hashed value for comparison (must be Base64 encoded)
     *
     * @return True if hashes match, false otherwise
     */
    public static boolean matchArgon2(String cleartext, String hash) {
        Arguments.requireNonBlank(cleartext, Required.CLEARTEXT);
        Arguments.requireNonBlank(hash, Required.HASH);

        var salt = Application.getInstance(Config.class).getString(Key.APPLICATION_SECRET);

        return Arrays.areEqual(hashArgon2(cleartext, salt).getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Hashes a given clear text data with SHA3-512
     * For simple hashing tasks
     * 
     * @param data The clear text data
     * @return SHA512 hashed value or null if hashing failed
     */
    public static String hexSHA512(String data) {
        Arguments.requireNonBlank(data, Required.DATA);

        try {
            var digest = MessageDigest.getInstance("SHA3-512");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Failed to create hash of data", e);
        }

        return null;
    }
    
    /**
     * Serializes an object into a Base64 encoded data string
     *
     * @param object The object to serialize
     * @return The base64 encoded data string
     */
    public static String serializeToBase64(Serializable object)  {
        Objects.requireNonNull(object, Required.OBJECT);
        
        byte[] serialize = FURY.serialize(object);
        return BASE64ENCODER.encodeToString(serialize);
    }
    
    /**
     * Deserializes a given Base64 encoded data string into an object
     * 
     * @param data The base64 encoded data string
     * @param <T> Just for JavaDoc can be ignored
     * @return The required object
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeFromBase64(String data) {
        Arguments.requireNonBlank(data, Required.DATA);
        
        byte[] bytes = BASE64DECODER.decode(data);
        return (T) FURY.deserialize(bytes);
    }
    
    /**
     * Encodes a given string to a Base64 byte array
     * 
     * @param data The data to convert
     * @return The converted byte array
     */
    public static byte[] encodeToBase64(String data) {
        Arguments.requireNonBlank(data, Required.DATA);
        return BASE64ENCODER.encode(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes a given string to a Base32 string
     *
     * @param data The data to convert
     * @return The converted byte array
     */
    public static String encodeToBase32(String data) {
        Arguments.requireNonBlank(data, Required.DATA);
        return BASE32ENCODER.encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes a given string to a Base64 byte array
     *
     * @param data The data to convert
     * @return The converted byte array
     */
    public static byte[] encodeToBase64(byte[] data) {
        Objects.requireNonNull(data, Required.DATA);
        return BASE64ENCODER.encode(data);
    }
    
    /**
     * Decodes a given Base64 encoded string to a byte array
     * 
     * @param data The data to convert
     * @return The converted byte array
     */
    public static byte[] decodeFromBase64(String data) {
        Arguments.requireNonBlank(data, Required.DATA);
        return BASE64DECODER.decode(data);
    }

    /**
     * Creates a UUIDv6 random String
     *
     * @return UUIDv6 String
     */
    public static String uuidV6() {
        return Generators.timeBasedReorderedGenerator().generate().toString();
    }

    /**
     * Creates a UUIDv4 random String
     *
     * @return UUIDv4 String
     */
    public static String uuidV4() {
        return Generators.randomBasedGenerator().generate().toString();
    }


    /**
     * Calculates the bit length of a given byte array
     *
     * @param bytes The byte array
     * @return The number of bit
     */
    public static int bitLength(byte[] bytes) {
        Objects.requireNonNull(bytes, Required.BYTES);
        int byteLength = bytes.length;

        var length = 0;
        if (byteLength <= MAX_BYTE_LENGTH && byteLength > 0) {
            length = byteLength * BYTES;
        }

        return length;
    }

    /**
     * Calculates the bit length of a given string
     *
     * @param string The string
     * @return The number of bit
     */
    public static int bitLength(String string) {
        Arguments.requireNonBlank(string, Required.STRING);
        return bitLength(string.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Copies a given map to a new map instance
     *
     * @param originalMap The map to copy
     * @return A new Map instance with value from originalMap
     */
    public static Map<String, String> copyMap(Map<String, String> originalMap) {
        Objects.requireNonNull(originalMap, Required.MAP);

        return new HashMap<>(originalMap);
    }

    /**
     * Copies a given map to a new map instance
     *
     * @param originalMap The map to copy
     * @return A new Map instance with value from originalMap
     */
    public static Map<String, String> toStringMap(Map<String, Object> originalMap) {
        Objects.requireNonNull(originalMap, Required.MAP);

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            result.put(key, value == null ? null : value.toString());
        }
        return result;
    }

    /**
     * Generates a random string with the given length.
     * <p>
     * Based on commons-lang3 RandomStringUtils using SecureRandom
     * <p>
     * Uses: uppercase letters, lowercase letters and numbers 0-9
     *
     * @param length The length of the random string
     * @return A random String
     */
    public static String randomString(int length) {
        Preconditions.checkArgument(length >= MIN_LENGTH, "Length must be at least " + MIN_LENGTH + " characters for security");
        Preconditions.checkArgument(length <= MAX_LENGTH, "Length must not exceed " + MAX_LENGTH + " characters");

        int bytesNeeded = (int) Math.ceil(length * 6 / 8.0);
        var randomBytes = new byte[bytesNeeded];
        SECURE_RANDOM.nextBytes(randomBytes);

        var token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return token.substring(0, length);
    }





    /**
     * Checks if a resource exists in the classpath
     *
     * @param name The name of the resource
     * @return True if the resources exists, false otherwise
     */
    public static boolean resourceExists(String name) {
        Arguments.requireNonBlank(name, Required.NAME);

        URL resource = null;
        try {
            resource = Resources.getResource(name);
        } catch (IllegalArgumentException e) { // NOSONAR Intentionally not logging or throwing this exception
            // Intentionally left blank
        }

        return resource != null;
    }



    /**
     * Reads the content of a local resource to String
     *
     * @param resource The resource path
     * @return The content of the resource or null
     */
    public static String readResourceToString(String resource) {
        Arguments.requireNonBlank(resource, Required.RESOURCE);

        var content = Strings.EMPTY;
        try {
            content = Resources.toString(Resources.getResource(resource), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Intentionally left blank
        }

        return content;
    }

    public static boolean isBlacklisted(String id) {
        Arguments.requireNonBlank(id, Required.ID);

        var authCache = Application
                .getInstance(CacheProvider.class)
                .getCache(CacheName.BLACKLIST);

        return authCache.get(Const.BLACKLIST_PREFIX + id) != null;
    }

    public static void blacklist(String id) {
        Arguments.requireNonBlank(id, Required.ID);

        var authCache = Application
                .getInstance(CacheProvider.class)
                .getCache(CacheName.BLACKLIST);

        authCache.put(Const.BLACKLIST_PREFIX + id, Strings.EMPTY);
    }
}