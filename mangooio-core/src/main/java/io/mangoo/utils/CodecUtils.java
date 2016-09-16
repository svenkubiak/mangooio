package io.mangoo.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.mindrot.jbcrypt.BCrypt;

import io.mangoo.enums.Default;

/**
 * 
 * @author svenkubiak
 *
 */
public final class CodecUtils {

    /**
     * Hashes a given cleartext data with JBCrypt
     * 
     * @param data The cleartext data
     * @return JBCrypted hashed value
     */
    public static String hexJBcrypt(String data) {
        return BCrypt.hashpw(data, BCrypt.gensalt(Default.JBCRYPT_ROUNDS.toInt()));
    }
    
    /**
     * Hashes a given cleartext data with SHA512
     * 
     * @param data The cleartext data
     * @return SHA512 hashed value
     */
    public static String hexSHA512(String data) {
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
        return BCrypt.checkpw(data, hash);
    }
}