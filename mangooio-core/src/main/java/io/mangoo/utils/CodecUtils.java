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

/**
 * 
 * @author svenkubiak
 *
 */
public final class CodecUtils {
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
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
        
        Argon2Parameters.Builder builder = (new Argon2Parameters.Builder())
                .withVersion(Argon2Parameters.ARGON2_id)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY)
                .withParallelism(PARALLELISM)
                .withSecret(password.getBytes(StandardCharsets.UTF_8))
                .withSalt(salt.getBytes(StandardCharsets.UTF_8));

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());

        byte[] passwdHash = new byte[32];
        generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), passwdHash);

        return base64Encoder.encodeToString(passwdHash);
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
     * Serializes an object into an Base64 encoded data string
     *
     * @param object The object to serialize
     * @return The base64 encoded data string
     */
    public static String serializeToBase64(Serializable object)  {
        Objects.requireNonNull(object, Required.OBJECT.toString());
        
        byte[] serialize = SerializationUtils.serialize(object);
        return base64Encoder.encodeToString(serialize);
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
        
        byte[] bytes = base64Decoder.decode(data);
        return SerializationUtils.deserialize(bytes);
    }
}