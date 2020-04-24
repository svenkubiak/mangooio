package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum ClaimKey {
    AUTHENTICITY("authenticity"),
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