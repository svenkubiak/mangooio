package io.mangoo.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 
 * @author svenkubiak
 *
 */
public final class TemplateUtils {
    public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(.*?)\\}");
    private static final List<String> blacklist = Arrays.asList(
            "form", "flash", "session", "subject", "i18n", "route", "location", "prettytime", "authenticity", "authenticityForm"
            );
    
    public static Optional<String> containsInvalidKey(Map<String, Object> content) {
        String found = null;
        for (String key : blacklist) {
            if (content.containsKey(key)) {
                found = key;
                break;
            }
        }
        
        return Optional.ofNullable(found);
    }
}