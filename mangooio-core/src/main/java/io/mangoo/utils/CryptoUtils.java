package io.mangoo.utils;

import java.security.SecureRandom;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.base.Preconditions;

/**
 *
 * @author svenkubiak
 *
 */
public final class CryptoUtils {
    private static final char[] CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int MAX_PASSWORD_LENGTH = 256;
    private static final int MIN_PASSWORD_LENGTH = 0;
    
    private CryptoUtils() {
    }
    
    /**
     * Generates a random string with the given length.
     * 
     * Based on commons-lang3 RandomStringUtils using SecureRandom
     * 
     * Uses: uppercase letters, lowercase letters and numbers
     * 
     * @param length The length of the random string
     * @return A random String
     */
    public static String randomString(int length) {
        Preconditions.checkArgument(length > MIN_PASSWORD_LENGTH, "random string length must be at least 1 character");
        Preconditions.checkArgument(length <= MAX_PASSWORD_LENGTH, "random string length must be at most 256 character");
        
        return RandomStringUtils.random(length, 0, CHARACTERS.length-1, false, false, CHARACTERS, new SecureRandom());
    }
}