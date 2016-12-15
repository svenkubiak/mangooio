package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum ClaimKey {
    VERSION("version"),
    DATA("data"),
    AUHTNETICITY("authenticity"),
    AUTHENTICATEDUSER("authenticatedUser"),
    FORM("form");
    
    private final String value;

    ClaimKey (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}