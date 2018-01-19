package io.mangoo.utils;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.enums.Required;

/**
 *
 * @author svenkubiak
 *
 */
public final class CryptoUtils {
    private static final int KEYINDEX_START = 0;
    private static final int KEYLENGTH = 32;

    private CryptoUtils() {
    }
    
    /**
     * Creates a secret for encrypt or decryption which has a length
     * of 32 characters, corresponding to 256 Bits
     * 
     * If the provided secret has more than 32 characters it will be trimmed
     * to 32 characters.
     *
     * @param secret A given secret to trim
     * @return A secret with at least 32 characters
     */
    public static String getSizedSecret(String secret) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        String key = StringUtils.replaceAll(secret, "[^\\x00-\\x7F]", "");
        
        return key.length() < KEYLENGTH ? key : key.substring(KEYINDEX_START, KEYLENGTH);
    }
    
    /**
     * Checks if the given secret has at least 32 characters
     * 
     * @param secret The secret to check
     * @return True if secret is at least 32 characters, false if not
     */
    public static boolean isValidSecret(String secret) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        String key = StringUtils.replaceAll(secret, "[^\\x00-\\x7F]", "");

        return key.length() >= KEYLENGTH;
    }
}
