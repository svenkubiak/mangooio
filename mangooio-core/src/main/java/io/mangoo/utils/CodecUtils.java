package io.mangoo.utils;

import java.io.Serializable;
import java.util.Base64;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;
import org.mindrot.jbcrypt.BCrypt;

import io.mangoo.enums.Default;
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
     * Hashes a given cleartext data with JBCrypt
     * 
     * @deprecated Use {@link #hashArgon2(String, String) hashArgon2} instead
     * @param data The cleartext data
     * @return JBCrypted hashed value
     */
    @Deprecated(since = "6.13.0", forRemoval = true)
    public static String hexJBcrypt(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        return BCrypt.hashpw(data, BCrypt.gensalt(Default.JBCRYPT_ROUNDS.toInt()));
    }
    
    public static String hashArgon2(String password, String salt) {
        Objects.requireNonNull(password, "password can not be null");
        Objects.requireNonNull(salt, "salt can not be null");
        
        Argon2Parameters.Builder builder = (new Argon2Parameters.Builder()).
                withVersion(Argon2Parameters.ARGON2_id)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY)
                .withParallelism(PARALLELISM)
                .withSecret(password.getBytes())
                .withSalt(salt.getBytes());

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());

        byte[] passwdHash = new byte[32];
        generator.generateBytes(password.getBytes(), passwdHash);

        return base64Encoder.encodeToString(passwdHash);
    }
    
    public static boolean matchArgon2(String password, String salt, String hashedPassword) {
        Objects.requireNonNull(password, "password can not be null");
        Objects.requireNonNull(salt, "salt can not be null");
        Objects.requireNonNull(hashedPassword, "hashedPassword can not be null");
        
        String hash = hashArgon2(password, salt);
        return Arrays.areEqual(hash.getBytes(), hashedPassword.getBytes());
    }
    
    /**
     * Hashes a given cleartext data with SHA512
     * For simple hashing tasks
     * Use {@link #hashArgon2(String, String) hashArgon2} for password hashing
     * 
     * @param data The cleartext data
     * @return SHA512 hashed value
     */
    public static String hexSHA512(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        return DigestUtils.sha512Hex(data);
    }
    
    /**
     * Hashes a given cleartext data with SHA512 and an appended salt
     * 
     * @param data The cleartext data
     * @param salt The salt to use
     * @return SHA512 hashed value
     */
    @Deprecated(since = "6.13.0", forRemoval = true)
    public static String hexSHA512(String data, String salt) {
        Objects.requireNonNull(data, Required.DATA.toString());
        Objects.requireNonNull(salt, Required.SALT.toString());
        
        return DigestUtils.sha512Hex(data + salt);
    }
    
    /**
     * Checks a given data against a JBCrypted hash
     * 
     * @deprecated Use {@link #matchArgon2(String, String, String) hashArgon2} instead
     * @param data The cleartext data
     * @param hash The JBCrypt hashed value
     * @return True if it is a match, false otherwise
     */
    @Deprecated(since = "6.13.0", forRemoval = true)
    public static boolean checkJBCrypt(String data, String hash) {
        Objects.requireNonNull(data, Required.DATA.toString());
        Objects.requireNonNull(hash, Required.HASH.toString());
        
        return BCrypt.checkpw(data, hash);
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
     * Deserialize a given Base64 encoded data string into an object
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