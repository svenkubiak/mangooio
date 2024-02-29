package io.mangoo.utils;

import io.mangoo.enums.Required;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ByteUtils {
    private static final int BYTES = 8;
    private static final int MAX_BYTE_LENGTH = Integer.MAX_VALUE / 8;
    
    private ByteUtils() {
    }
    
    /**
     * Calculates the bit length of a given byte array
     * 
     * @param bytes The byte array
     * @return The number of bit
     */
    public static int bitLength(byte[] bytes) {
        Objects.requireNonNull(bytes, Required.BYTES.toString());
        int byteLength = bytes.length;
        
        var length = 0;
        if (byteLength <= MAX_BYTE_LENGTH && byteLength > 0) {
            length = byteLength * BYTES;
        }
        
        return length;
    }
    
    /**
     * Calculates the bit length of a given string
     * 
     * @param string The string
     * @return The number of bit
     */
    public static int bitLength(String string) {
        Objects.requireNonNull(string, Required.STRING.toString());
        return bitLength(string.getBytes(StandardCharsets.UTF_8));
    }
}