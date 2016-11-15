package io.mangoo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import com.google.common.base.Charsets;

import io.mangoo.crypto.Crypto;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
public final class CodecUtils {
    private static final Logger LOG = LogManager.getLogger(Crypto.class);
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
        
        String encoded = null;
        try {
            encoded = new String(base64Encoder.encode(data.getBytes("UTF-8")), Charsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to encode data string: " + data,  e);
        }
        
        return encoded;
    }
    
    /**
     * Decodes a given String of data to Base64
     * 
     * @param data The String to convert
     * @return Base64 encoded String
     */
    public static String decodeBase64(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        String decoded = null;
        try {
            decoded = new String(base64Decoder.decode(data.getBytes("UTF-8")), Charsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to encode data string: " + data,  e);
        }
        
        return decoded;
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
    
    /**
     * Deserialize a given Base64 encoded data string into an object
     * 
     * @param data The base64 encoded data string
     * @return The required object
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeFromString(String data) {
        Objects.requireNonNull(data, Required.DATA.toString());
        
        byte[] bytes = Base64.getDecoder().decode(data);
        Object object = null;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));) {
            object = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Failed to deserialize object from string: " + data, e);
        }

        return (object != null) ? (T) object : null;
    }

    /**
     * Serializes an object into an Base64 encoded data string
     * 
     * @param object The object to serialize
     * @return The base64 encoded data string
     */
    public static String serializeToString(Serializable object)  {
        Objects.requireNonNull(object, Required.OBJECT.toString());
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);) {
            objectOutputStream.writeObject(object);
        } catch (IOException e) {
            LOG.error("Failed to serialize object: " + object, e);
        }

        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }
}