package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum Claims {
    VERSION("version"),
    DATA("data"),
    AUHTNETICITYTOKEN("authenticityToken"),
    AUTHENTICATEDUSER("authenticatedUser");

    private final String value;

    Claims (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}