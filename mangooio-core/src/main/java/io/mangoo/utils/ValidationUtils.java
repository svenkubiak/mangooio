package io.mangoo.utils;

import java.util.HashMap;
import java.util.Map;

import io.mangoo.enums.Key;
import io.mangoo.enums.Validation;

/**
 * 
 * @author svenkubiak
 *
 */
public final class ValidationUtils {
    private static final Map<String, String> defaults;
    static
    {
        defaults = new HashMap<>();
        defaults.put(Key.VALIDATION_REQUIRED.toString(), Validation.REQUIRED.toString());
        defaults.put(Key.VALIDATION_MIN.toString(), Validation.MIN.toString());
        defaults.put(Key.VALIDATION_MAX.toString(), Validation.MAX.toString());
        defaults.put(Key.VALIDATION_EXACT_MATCH.toString(), Validation.EXACT_MATCH.toString());
        defaults.put(Key.VALIDATION_MATCH.toString(), Validation.MATCH.toString());
        defaults.put(Key.VALIDATION_EMAIL.toString(), Validation.EMAIL.toString());
        defaults.put(Key.VALIDATION_IPV4.toString(), Validation.IPV4.toString());
        defaults.put(Key.VALIDATION_IPV6.toString(), Validation.IPV6.toString());
        defaults.put(Key.VALIDATION_RANGE.toString(), Validation.RANGE.toString());
        defaults.put(Key.VALIDATION_URL.toString(), Validation.URL.toString());
        defaults.put(Key.VALIDATION_REGEX.toString(), Validation.REGEX.toString());
        defaults.put(Key.VALIDATION_NUMERIC.toString(), Validation.NUMERIC.toString());
    }
    
    private ValidationUtils() {
    }
    
    public static Map<String, String> getDefaults() {
        return defaults;
    }
}