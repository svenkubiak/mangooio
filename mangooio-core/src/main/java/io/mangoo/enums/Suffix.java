package io.mangoo.enums;

/**
 * Application modes
 *
 * @author svenkubiak
 *
 */
public enum Suffix {
    CSS(".css"),
    JS(".js"),
    CSS_MIN(".min.css"),
    JS_MIN(".min.js");

    private final String value;

    Suffix (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}