package io.mangoo.enums;

/**
 * Default validation messages
 *
 * @author svenkubiak
 *
 */
public enum Validation {
    REQUIRED("{0} is required"),
    MIN("{0} must be at least {1} characters"),
    MAX("{0} can be max {1} characters"),
    EXACT_MATCH("{0} must exactly match {1}"),
    MATCH("{0} must match {1}"),
    EMAIL("{0} must be a valid eMail address"),
    IPV4("{0} must be a valid IPv4 address"),
    IPV6("{0} must be a valid IPv6 address"),
    RANGE("{0} must be between {1} and {2} characters"),
    URL("{0} must be a valid URL");

    private final String value;

    Validation (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}