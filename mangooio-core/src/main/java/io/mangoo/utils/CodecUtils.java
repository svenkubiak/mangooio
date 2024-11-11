package io.mangoo.utils;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedReorderedGenerator;
import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import org.apache.commons.codec.binary.Hex;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

public final class CodecUtils {
    private static final Logger LOG = LogManager.getLogger(CodecUtils.class);
    private static final Base64.Encoder BASE64ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64DECODER = Base64.getDecoder();
    private static final TimeBasedReorderedGenerator UUID_GENERATOR = Generators.timeBasedReorderedGenerator();
    private static final ThreadSafeFury FURY = Fury.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(false)
            .buildThreadSafeFury();

    private CodecUtils() {
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
        Objects.requireNonNull(cleartext, NotNull.CLEARTEXT);
        Objects.requireNonNull(salt, NotNull.SALT);

        var argon2 = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withParallelism(2)
                .withMemoryAsKB(100000)
                .withSalt(salt.getBytes(StandardCharsets.UTF_8))
                .withIterations(16)
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
        Objects.requireNonNull(cleartext, NotNull.CLEARTEXT);

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
        Objects.requireNonNull(cleartext, NotNull.CLEARTEXT);
        Objects.requireNonNull(salt, NotNull.SALT);
        Objects.requireNonNull(hash, NotNull.HASH);

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
        Objects.requireNonNull(cleartext, NotNull.CLEARTEXT);
        Objects.requireNonNull(hash, NotNull.HASH);

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
        Objects.requireNonNull(data, NotNull.DATA);

        try {
            var digest = MessageDigest.getInstance("SHA3-512");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return Hex.encodeHexString(hashBytes);
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
        Objects.requireNonNull(object, NotNull.OBJECT);
        
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
        Objects.requireNonNull(data, NotNull.DATA);
        
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
        Objects.requireNonNull(data, NotNull.DATA);
        return BASE64ENCODER.encode(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes a given string to a Base64 byte array
     *
     * @param data The data to convert
     * @return The converted byte array
     */
    public static byte[] encodeToBase64(byte[] data) {
        Objects.requireNonNull(data, NotNull.DATA);
        return BASE64ENCODER.encode(data);
    }
    
    /**
     * Decodes a given Base64 encoded string to a byte array
     * 
     * @param data The data to convert
     * @return The converted byte array
     */
    public static byte[] decodeFromBase64(String data) {
        Objects.requireNonNull(data, NotNull.DATA);
        return BASE64DECODER.decode(data);
    }

    /**
     * Creates a UUIDv6 random String
     *
     * @return UUIDv6 String
     */
    public static String uuid() {
        return UUID_GENERATOR.generate().toString();
    }
}