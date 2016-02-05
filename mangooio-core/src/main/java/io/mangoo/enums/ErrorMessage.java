package io.mangoo.enums;

/**
 * Error messages
 *
 * @author svenkubiak
 *
 */
public enum ErrorMessage {
    URI("uri can not be null");

    private final String value;

    ErrorMessage (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}