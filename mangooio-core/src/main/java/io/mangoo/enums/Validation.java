package io.mangoo.enums;

/**
 * Default validation messages
 *
 * @author svenkubiak
 *
 */
public enum Validation {
    DOMAIN_NAME("{0} must be a valid domain name"),
    EMAIL("{0} must be a valid eMail address"),
    EXACT_MATCH("{0} must exactly match {1}"),
    IPV4("{0} must be a valid IPv4 address"),
    IPV6("{0} must be a valid IPv6 address"),
    MACTH_VALUES("The values of {0} is not valid"),
    MATCH("{0} must match {1}"),
    MAX("{0} must have a size of max {1}"),
    MIN("{0} must have a least a size of {1}"),
    NUMERIC("{0} must be a numeric value"),
    RANGE("{0} must have a size between {1} and {2}"),
    REGEX("{0} is invalid"),
    REQUIRED("{0} is required"),
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