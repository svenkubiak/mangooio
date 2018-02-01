package io.mangoo.enums;

/**
 * Default validation messages
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("all")
public enum Validation {
    DOMAIN_NAME("{0} must be a valid domain name"),
    DOMAIN_NAME_KEY("validation.domainname"),
    EMAIL("{0} must be a valid eMail address"),
    EMAIL_KEY("validation.email"),
    EXACT_MATCH("{0} must exactly match {1}"),
    EXACT_MATCH_KEY("validation.exactmatch"),
    FALSE("{0} is not a valid value"),
    FALSE_KEY("validation.false"),
    IPV4("{0} must be a valid IPv4 address"),
    IPV4_KEY("validation.ipv4"),
    IPV6("{0} must be a valid IPv6 address"),
    IPV6_KEY("validation.ipv6"),
    MATCH("{0} must match {1}"),
    MATCH_KEY("validation.match"),
    MATCH_VALUES("The values of {0} is not valid"),
    MATCH_VALUES_KEY("validation.matchvalues"),
    MAX("{0} must have a size of max {1}"),
    MAX_KEY("validation.max"),
    MIN("{0} must have a least a size of {1}"),
    MIN_KEY("validation.min"),
    NOTNULL("{0} is not a valid value"),
    NOTNULL_KEY("validation.notnull"),
    NULL("{0} is not a valid value"),
    NULL_KEY("validation.null"),
    NUMERIC("{0} must be a numeric value"),
    NUMERIC_KEY("validation.numeric"),
    RANGE("{0} must have a size between {1} and {2}"),
    RANGE_KEY("validation.range"),
    REGEX("{0} is invalid"),
    REGEX_KEY("validation.regex"),
    REQUIRED("{0} is required"),
    REQUIRED_KEY("validation.required"),
    TRUE("{0} is not a valid value"),
    TRUE_KEY("validation.true"),
    URL("{0} must be a valid URL"),
    URL_KEY("validation.url");

    private final String value;

    Validation (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}