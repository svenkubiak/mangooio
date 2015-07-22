package io.mangoo.enums;

/**
 * Application modes
 *
 * @author svenkubiak
 *
 */
public enum Mode {
    DEV("dev"),
    TEST("test"),
    PROD("prod");

    private final String value;

    Mode (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}