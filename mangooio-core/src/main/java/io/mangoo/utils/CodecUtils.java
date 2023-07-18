package io.mangoo.utils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;

import io.mangoo.enums.Required;

public final class CodecUtils {
    private static final Base64.Encoder BASRE64ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64DECODER = Base64.getDecoder();
    private static final int ITERATIONS = 20;
    private static final int MEMORY = 16777;
    private static final int PARALLELISM = 4;
    
    private CodecUtils() {
    }
    
    /**
     * Hashes a given clear text password with salt using Argon2Id password hashing
     * 
     * @param password The clear text password
     * @param salt The salt
     * 
     * @return The hashed password
     */
    public static String hashArgon2(String password, String salt) {
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        Objects.requireNonNull(salt, Required.SALT.toString());
        
        var aargon2Builder = (new Argon2Parameters.Builder())
                .withVersion(Argon2Parameters.ARGON2_id)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY)
                .withParallelism(PARALLELISM)
                .withSecret(password.getBytes(StandardCharsets.UTF_8))
                .withSalt(salt.getBytes(StandardCharsets.UTF_8));

        var aargon2Generator = new Argon2BytesGenerator();
        aargon2Generator.init(aargon2Builder.build());

        var passwdHash = new byte[32];
        aargon2Generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), passwdHash);

        return BASRE64ENCODER.encodeToString(passwdHash);
    }
    
    /**
     * Matches a given clear text password with salt using Argon2Id against an already
     * Argon2Id hashed password
     * 
     * @param password The clear text password
     * @param salt The salt
     * @param hashedPassword The already hashed password
     * 
     * @return True if hashes match, false otherwise
     */
    public static boolean matchArgon2(String password, String salt, String hashedPassword) {
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        Objects.requireNonNull(salt, Required.SALT.toString());
        Objects.requireNonNull(hashedPassword, Required.PASSWORD.toString());
        
        return Arrays.areEqual(hashArgon2(password, salt).getBytes(StandardCharsets.UTF_8), hashedPassword.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Hashes a given clear text data with SHA512
     * For simple hashing tasks
     * Use {@link #hashArgon2(String, String) hashArgon2} for password hashing
     * 
     * @param data The clear text data
     * @return SHA512 hashed value
     */
    public static String hexSHA512(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        return DigestUtils.sha512Hex(data);
    }
    
    /**
     * Serializes an object into a Base64 encoded data string
     *
     * @param object The object to serialize
     * @return The base64 encoded data string
     */
    public static String serializeToBase64(Serializable object)  {
        Objects.requireNonNull(object, Required.OBJECT.toString());
        
        byte[] serialize = SerializationUtils.serialize(object);
        return BASRE64ENCODER.encodeToString(serialize);
    }
    
    /**
     * Deserializes a given Base64 encoded data string into an object
     * 
     * @param data The base64 encoded data string
     * @param <T> Just for JavaDoc can be ignored
     * @return The required object
     */
    public static <T> T deserializeFromBase64(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        byte[] bytes = BASE64DECODER.decode(data);
        return SerializationUtils.deserialize(bytes);
    }
    
    /**
     * Encodes a given string to a Base64 byte array
     * 
     * @param data The data to convert
     * @return The converted byte array
     */
    public static byte[] encodeBase64(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        return BASRE64ENCODER.encode(data.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Decodes a given Base64 encoded string to a byte array
     * 
     * @param data The data to convert
     * @return The converted byte array
     */
    public static byte[] decodeBase64(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        return BASE64DECODER.decode(data);
    }
}