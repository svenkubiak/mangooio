package io.mangoo.enums;

public enum ClaimKey {
    DATA("data"),
    FORM("form"),
    TWO_FACTOR("twoFactor");
    
    private final String value;

    ClaimKey (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}