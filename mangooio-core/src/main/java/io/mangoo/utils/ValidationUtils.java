package io.mangoo.utils;

import java.util.HashMap;
import java.util.Map;

import io.mangoo.enums.Validation;

/**
 * 
 * @author svenkubiak
 *
 */
public final class ValidationUtils {
    private static final Map<String, String> defaults;
    static {
        defaults = new HashMap<>();
        defaults.put(Validation.REQUIRED_KEY.name(), Validation.REQUIRED.toString());
        defaults.put(Validation.MIN_KEY.name(), Validation.MIN.toString());
        defaults.put(Validation.MAX_KEY.name(), Validation.MAX.toString());
        defaults.put(Validation.EXACT_MATCH_KEY.name(), Validation.EXACT_MATCH.toString());
        defaults.put(Validation.MATCH_KEY.name(), Validation.MATCH.toString());
        defaults.put(Validation.EMAIL_KEY.name(), Validation.EMAIL.toString());
        defaults.put(Validation.IPV4_KEY.name(), Validation.IPV4.toString());
        defaults.put(Validation.IPV6_KEY.name(), Validation.IPV6.toString());
        defaults.put(Validation.RANGE_KEY.name(), Validation.RANGE.toString());
        defaults.put(Validation.URL_KEY.name(), Validation.URL.toString());
        defaults.put(Validation.MATCH_VALUES_KEY.name(), Validation.MATCH_VALUES.toString());
        defaults.put(Validation.REGEX_KEY.name(), Validation.REGEX.toString());
        defaults.put(Validation.NUMERIC_KEY.name(), Validation.NUMERIC.toString());
        defaults.put(Validation.DOMAIN_NAME_KEY.name(), Validation.DOMAIN_NAME.toString());
    }
    
    private ValidationUtils() {
    }
    
    public static Map<String, String> getDefaults() {
        return defaults;
    }
}