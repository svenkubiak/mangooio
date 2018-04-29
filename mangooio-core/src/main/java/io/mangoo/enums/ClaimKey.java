package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum ClaimKey {
    AUTHENTICATEDUSER("authenticatedUser"),
    AUTHENTICITY("authenticity"),
    DATA("data"),
    EXPIRES("expires"),
    FORM("form"),
    TWO_FACTOR("twoFactor"),
    VERSION("version");
    
    private final String value;

    ClaimKey (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}