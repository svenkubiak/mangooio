package io.mangoo.enums;

public enum Mode {
    DEV("dev"),
    PROD("prod"),
    TEST("test");

    private final String value;

    Mode (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}