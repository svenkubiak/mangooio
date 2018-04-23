package io.mangoo.utils;

import java.util.Objects;

import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
public final class ByteUtils {
    private static final int MAX_BYTE_LENGTH = Integer.MAX_VALUE / 8;
    
    /**
     * Calculates the bit length of a given byte array
     * 
     * @param bytes The byte array
     * @return The number of bit
     */
    public static int bitLength (byte[] bytes) {
        Objects.requireNonNull(bytes, Required.BYTES.toString());
        int byteLength = bytes.length;
        
        int length = 0;
        if (byteLength <= MAX_BYTE_LENGTH && byteLength > 0) {
            length = byteLength * 8;
        }
        
        return length;
    }
    
    /**
     * Calculates the bit length of a given string
     * 
     * @param string The string
     * @return The number of bit
     */
    public static int bitLength (String string) {
        Objects.requireNonNull(string, Required.STRING.toString());
        int byteLength = string.getBytes().length;
        
        int length = 0;
        if (byteLength <= MAX_BYTE_LENGTH && byteLength > 0) {
            length = byteLength * 8;
        }
        
        return length;
    }
}
