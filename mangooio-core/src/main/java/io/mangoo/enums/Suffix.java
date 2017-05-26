package io.mangoo.enums;

/**
 * Application modes
 *
 * @author svenkubiak
 *
 */
public enum Suffix {
    CSS(".css"),
    CSS_MIN(".min.css"),
    JS(".js"),
    JS_MIN(".min.js"),
    LESS(".less"),
    SASS(".sass");

    private final String value;

    Suffix (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}