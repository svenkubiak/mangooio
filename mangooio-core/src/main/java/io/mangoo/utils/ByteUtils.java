package io.mangoo.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Charsets;

import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
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
    public static int bitLength (byte[] bytes) {
        Objects.requireNonNull(bytes, Required.BYTES.toString());
        int byteLength = bytes.length;
        
        int length = 0;
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
    public static int bitLength (String string) {
        Objects.requireNonNull(string, Required.STRING.toString());
        int byteLength = string.getBytes(Charsets.UTF_8).length;
        
        int length = 0;
        if (byteLength <= MAX_BYTE_LENGTH && byteLength > 0) {
            length = byteLength * BYTES;
        }
        
        return length;
    }
    
    /**
     * Copies a given map to a new map instance
     * 
     * @param originalMap The map to copy
     * @return A new Map instance with value from originalMap
     */
    public static Map<String, String> copyMap(Map<String, String> originalMap) {
        Objects.requireNonNull(originalMap, Required.MAP.toString());
        
        return new HashMap<>(originalMap);
    }
}
