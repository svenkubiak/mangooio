package io.mangoo.utils;

import com.fasterxml.uuid.Generators;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import io.mangoo.constants.Key;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.crypto.PasswordHasher;
import io.mangoo.exceptions.MangooHashingException;
import io.mangoo.interfaces.TokenBlacklist;
import org.apache.commons.codec.binary.Base32;
import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

public final class CommonUtils {
    private static final Logger LOG = LogManager.getLogger(CommonUtils.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MAX_LENGTH = 512;
    private static final int MIN_LENGTH = 22;
    private static final Base32 BASE32_ENCODER = new Base32();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final int BYTES = 8;
    private static final int MAX_BYTE_LENGTH = Integer.MAX_VALUE / 8;
    private static final ThreadSafeFory FORY = Fory.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(true)
            .buildThreadSafeFory();

    private CommonUtils() {
    }

    /**
     * Must be called during application startup before first use; only deserialize data from trusted sources.
     */
    public static void registerSerializable(Class<?> type) {
        Objects.requireNonNull(type, Required.CLASS);
        FORY.register(type);
    }
    
    /**
     * Returns an Argon2id hash in the PHC string format.
     * Throws {@link MangooHashingException} if no hashing slot becomes available within authentication.hashing.timeout.
     */
    public static String hashArgon2(String cleartext, String salt) {
        Argument.requireNonBlank(cleartext, Required.CLEARTEXT);
        Argument.requireNonBlank(salt, Required.SALT);

        return Application.getInstance(PasswordHasher.class).hash(cleartext, salt);
    }

    /**
     * Uses the application secret as salt; throws MangooHashingException if no hashing slot becomes available in time.
     */
    public static String hashArgon2(String cleartext) {
        Argument.requireNonBlank(cleartext, Required.CLEARTEXT);

        var salt = Application.getInstance(Config.class).getString(Key.APPLICATION_SECRET);
        return hashArgon2(cleartext, salt);
    }
    
    /**
     * PHC hashes are verified with their embedded parameters, legacy non-PHC hashes with the former defaults (m=80000, t=6, p=2) and the given salt.
     * Throws {@link MangooHashingException} instead of returning false if no hashing slot becomes available in time.
     */
    public static boolean matchArgon2(String cleartext, String salt, String hash) {
        Argument.requireNonBlank(cleartext, Required.CLEARTEXT);
        Argument.requireNonBlank(salt, Required.SALT);
        Argument.requireNonBlank(hash, Required.HASH);

        return Application.getInstance(PasswordHasher.class).matches(cleartext, salt, hash);
    }

    /**
     * Uses the application secret as salt, otherwise behaves like {@link #matchArgon2(String, String, String)}.
     */
    public static boolean matchArgon2(String cleartext, String hash) {
        Argument.requireNonBlank(cleartext, Required.CLEARTEXT);
        Argument.requireNonBlank(hash, Required.HASH);

        var salt = Application.getInstance(Config.class).getString(Key.APPLICATION_SECRET);

        return matchArgon2(cleartext, salt, hash);
    }

    /**
     * True for legacy non-PHC hashes and for hashes whose parameters differ from the current configuration.
     * Only evaluate after matchArgon2 returned true, as a failed login must never trigger a rehash.
     */
    public static boolean needsRehash(String hash) {
        Argument.requireNonBlank(hash, Required.HASH);

        return Application.getInstance(PasswordHasher.class).needsRehash(hash);
    }
    
    /**
     * Uses SHA3-512; returns null if hashing failed.
     */
    public static String hexSHA512(String data) {
        Argument.requireNonBlank(data, Required.DATA);

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
     * The object's class must be registered via {@link #registerSerializable(Class)} first.
     */
    public static String serializeToBase64(Serializable object)  {
        Objects.requireNonNull(object, Required.OBJECT);
        
        byte[] serialize = FORY.serialize(object);
        return BASE64_ENCODER.encodeToString(serialize);
    }
    
    /**
     * Only use with trusted data; the object's class must be registered via {@link #registerSerializable(Class)} first.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeFromBase64(String data) {
        Argument.requireNonBlank(data, Required.DATA);
        
        byte[] bytes = BASE64_DECODER.decode(data);
        return (T) FORY.deserialize(bytes);
    }
    
    public static byte[] encodeToBase64(String data) {
        Argument.requireNonBlank(data, Required.DATA);
        return BASE64_ENCODER.encode(data.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] urlEncodeToBase64(String data) {
        Argument.requireNonBlank(data, Required.DATA);
        return BASE64_URL_ENCODER.encode(data.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] urlEncodeWithoutPaddingToBase64(String data) {
        Argument.requireNonBlank(data, Required.DATA);
        return BASE64_URL_ENCODER.withoutPadding().encode(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String urlEncodeWithoutPaddingToBase64(byte [] data) {
        Objects.requireNonNull(data, Required.DATA);
        return BASE64_URL_ENCODER.withoutPadding().encodeToString(data);
    }

    public static byte[] urlDecodeFromBase64(byte[] data) {
        Objects.requireNonNull(data, Required.DATA);
        return BASE64_URL_DECODER.decode(data);
    }

    public static byte[] urlDecodeFromBase64(String data) {
        Argument.requireNonBlank(data, Required.DATA);
        return BASE64_URL_DECODER.decode(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeToBase32(String data) {
        Argument.requireNonBlank(data, Required.DATA);
        return BASE32_ENCODER.encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] encodeToBase64(byte[] data) {
        Objects.requireNonNull(data, Required.DATA);
        return BASE64_ENCODER.encode(data);
    }
    
    public static byte[] decodeFromBase64(String data) {
        Argument.requireNonBlank(data, Required.DATA);
        return BASE64_DECODER.decode(data);
    }

    public static String uuidV6() {
        return Generators.timeBasedReorderedGenerator().generate().toString();
    }

    public static String uuidV7() {
        return Generators.timeBasedEpochRandomGenerator().generate().toString();
    }

    public static String uuidV4() {
        return Generators.randomBasedGenerator().generate().toString();
    }


    public static int bitLength(byte[] bytes) {
        Objects.requireNonNull(bytes, Required.BYTES);
        int byteLength = bytes.length;



        var length = 0;
        if (byteLength <= MAX_BYTE_LENGTH && byteLength > 0) {
            length = byteLength * BYTES;
        }

        return length;
    }

    public static int bitLength(String string) {
        Argument.requireNonBlank(string, Required.STRING);
        return bitLength(string.getBytes(StandardCharsets.UTF_8));
    }

    public static Map<String, String> copyMap(Map<String, String> originalMap) {
        Objects.requireNonNull(originalMap, Required.MAP);

        return new HashMap<>(originalMap);
    }

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
     * Uses SecureRandom and the URL-safe Base64 alphabet.
     */
    public static String randomString(int length) {
        Preconditions.checkArgument(length >= MIN_LENGTH, "Length must be at least " + MIN_LENGTH + " characters for security");
        Preconditions.checkArgument(length <= MAX_LENGTH, "Length must not exceed " + MAX_LENGTH + " characters");

        int bytesNeeded = (int) Math.ceil(length * 6 / 8.0);
        var randomBytes = new byte[bytesNeeded];
        SECURE_RANDOM.nextBytes(randomBytes);

        var token = BASE64_URL_ENCODER.withoutPadding().encodeToString(randomBytes);
        return token.substring(0, length);
    }

    public static boolean resourceExists(String name) {
        Argument.requireNonBlank(name, Required.NAME);

        URL resource = null;
        try {
            resource = Resources.getResource(name);
        } catch (IllegalArgumentException e) { // NOSONAR Intentionally not logging or throwing this exception
            // A missing resource is reported as false
        }

        return resource != null;
    }

    /**
     * Returns an empty string if the resource cannot be read.
     */
    public static String readResourceToString(String resource) {
        Argument.requireNonBlank(resource, Required.RESOURCE);

        var content = Strings.EMPTY;
        try {
            content = Resources.toString(Resources.getResource(resource), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // An unreadable resource yields an empty string
        }

        return content;
    }

    /**
     * @deprecated Use {@link TokenBlacklist#isRevoked(String, String, Instant)} instead
     */
    @Deprecated(since = "10.14.0", forRemoval = true)
    public static boolean isBlacklisted(String id) {
        Argument.requireNonBlank(id, Required.ID);

        return Application.getInstance(TokenBlacklist.class).isRevoked(id, null, null);
    }

    /**
     * @deprecated Use {@link TokenBlacklist#revoke(String, Instant)} or {@link TokenBlacklist#revokeSubject(String)} instead
     */
    @Deprecated(since = "10.14.0", forRemoval = true)
    public static void blacklist(String id) {
        Argument.requireNonBlank(id, Required.ID);

        var config = Application.getInstance(Config.class);
        long maxTokenLifetime = Math.max(config.getAuthenticationCookieRememberExpires(), config.getAuthenticationCookieTokenExpires());

        Application.getInstance(TokenBlacklist.class).revoke(id, Instant.now().plusSeconds(maxTokenLifetime));
    }
}