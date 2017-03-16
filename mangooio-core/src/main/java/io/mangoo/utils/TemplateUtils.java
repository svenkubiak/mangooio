package io.mangoo.utils;

import java.util.regex.Pattern;

/**
 * 
 * @author svenkubiak
 *
 */
public final class TemplateUtils {
    public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(.*?)\\}");
}