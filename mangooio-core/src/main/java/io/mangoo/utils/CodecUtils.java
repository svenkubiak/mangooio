package io.mangoo.utils;

import java.util.Base64;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.mindrot.jbcrypt.BCrypt;

import com.google.common.base.Charsets;

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
    
    private CodecUtils() {
    }

    /**
     * Hashes a given cleartext data with JBCrypt
     * 
     * @param data The cleartext data
     * @return JBCrypted hashed value
     */
    public static String hexJBcrypt(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        return BCrypt.hashpw(data, BCrypt.gensalt(Default.JBCRYPT_ROUNDS.toInt()));
    }
    
    /**
     * Hashes a given cleartext data with SHA512
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
    public static String hexSHA512(String data, String salt) {
        Objects.requireNonNull(data, Required.DATA.toString());
        Objects.requireNonNull(salt, Required.SALT.toString());
        
        return DigestUtils.sha512Hex(data + salt);
    }
    
    /**
     * Checks a given data against a JBCrypted hash
     * 
     * @param data The cleartext data
     * @param hash The JBCrypt hashed value
     * @return True if it is a match, false otherwise
     */
    public static boolean checkJBCrypt(String data, String hash) {
        Objects.requireNonNull(data, Required.DATA.toString());
        Objects.requireNonNull(hash, Required.HASH.toString());
        
        return BCrypt.checkpw(data, hash);
    }
    
    /**
     * Encodes a given String of data to Base64
     * 
     * @param data The String to convert
     * @return Base64 encoded String
     */
    public static String encodeBase64(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        return new String(base64Encoder.encode(data.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
    }
    
    /**
     * Decodes a given String of data to Base64
     * 
     * @param data The String to convert
     * @return Base64 encoded String
     */
    public static String decodeBase64(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        return new String(base64Decoder.decode(data.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
    }
    
    /**
     * Encodes a given byte array of data to Base64
     * 
     * @param data The String to convert
     * @return Base64 encoded String
     */
    public static String encodeBase64(byte[] data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        return new String(base64Encoder.encode(data), Charsets.UTF_8);
    }
    
    /**
     * Decodes a given byte array of data to Base64
     * 
     * @param data The String to convert
     * @return Base64 encoded String
     */
    public static String decodeBase64(byte[] data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        return new String(base64Decoder.decode(data), Charsets.UTF_8);
    }
}