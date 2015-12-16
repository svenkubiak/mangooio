package io.mangoo.enums;

/**
 * Default validation messages
 *
 * @author svenkubiak
 *
 */
public enum Validation {
    REQUIRED("{0} is required"),
    MIN("{0} must have a least a size of {1}"),
    MAX("{0} must have a size of max {1}"),
    EXACT_MATCH("{0} must exactly match {1}"),
    MATCH("{0} must match {1}"),
    REGEX("{0} is invalid"),
    EMAIL("{0} must be a valid eMail address"),
    IPV4("{0} must be a valid IPv4 address"),
    IPV6("{0} must be a valid IPv6 address"),
    RANGE("{0} must have a size between {1} and {2}"),
    URL("{0} must be a valid URL"),
    NUMERIC("{0} must be a numeric value");

    private final String value;

    Validation (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}