package io.mangoo.utils;

import io.mangoo.constants.NotNull;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.Language;
import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public final class CodecUtils {
    private static final Base64.Encoder BASE64ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64DECODER = Base64.getDecoder();
    private static final byte[] NAMESPACE = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
            .toString()
            .getBytes(StandardCharsets.UTF_8);
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
                .withParallelism(1)
                .withMemoryAsKB(65536)
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
     * Matches a given clear text  with salt using Argon2Id against an already Argon2Id hashed value
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
     * Hashes a given clear text data with SHA512
     * For simple hashing tasks
     * 
     * @param data The clear text data
     * @return SHA512 hashed value
     */
    public static String hexSHA512(String data) {
        Objects.requireNonNull(data, NotNull.DATA);

        return DigestUtils.sha512Hex(data);
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
     * Creates a UUIDv5 random String
     *
     * @return UUIDv5 String or an empty String if generation fails
     */
    public static String uuid() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(NAMESPACE);

            byte[] data = messageDigest.digest();
            data[6] = (byte) (data[6] & 0x0f);
            data[6] = (byte) (data[6] | 0x50); // set version 5
            data[8] = (byte) (data[8] & 0x3f);
            data[8] = (byte) (data[8] | 0x80);

            long msb = 0L;
            long lsb = 0L;

            for (int i = 0; i <= 7; i++) {
                msb = (msb << 8) | (data[i] & 0xff);
            }

            for (int i = 8; i <= 15; i++) {
                lsb = (lsb << 8) | (data[i] & 0xff);
            }

            long mostSigBits = msb;
            long leastSigBits = lsb;

            return new UUID(mostSigBits, leastSigBits).toString();
        } catch (NoSuchAlgorithmException e) {
            //Intentionally left blank
        }

        return Strings.EMPTY;
    }
}