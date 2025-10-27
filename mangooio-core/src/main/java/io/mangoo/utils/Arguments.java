package io.mangoo.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Arguments {
    private Arguments() {}

    public static String requireNonBlank(String string, String message) {
        if (StringUtils.isBlank(string)) {
            throw new IllegalArgumentException(message);
        }
        return string;
    }

    public static void requireNonBlank(String message, String... strings) {
        for (String s : strings) {
            if (StringUtils.isBlank(s)) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    public static void validate(String string, Pattern pattern) {
        Arguments.requireNonBlank(string, "string can not be null or blank");
        Objects.requireNonNull(pattern, "pattern can not be null");

        if (!pattern.matcher(string).matches()) {
            throw new IllegalArgumentException("Invalid argument");
        }
    }
}
