package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum ClaimKey {
    AUTHENTICITY("authenticity"),
    AUTHENTICATEDUSER("authenticatedUser"),
    DATA("data"),
    FORM("form"),
    TWO_FACTOR("twoFactor"),
    VERSION("version");
    
    private final String value;

    private ClaimKey (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}