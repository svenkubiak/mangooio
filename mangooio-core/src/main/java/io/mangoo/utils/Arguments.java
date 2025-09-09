package io.mangoo.utils;

import org.apache.commons.lang3.StringUtils;

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
}
