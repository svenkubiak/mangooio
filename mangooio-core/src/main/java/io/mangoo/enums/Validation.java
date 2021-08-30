package io.mangoo.enums;

/**
 * Default validation messages
 *
 * @author svenkubiak
 *
 */
public enum Validation {
    DOMAIN_NAME("{0} must be a valid domain name"),
    DOMAIN_NAME_KEY("validation.domainname"),
    EMAIL("{0} must be a valid eMail address"),
    EMAIL_KEY("validation.email"),
    EXACT_MATCH("{0} must exactly match {1}"),
    EXACT_MATCH_KEY("validation.exactmatch"),
    FALSE("{0} must be false"),
    FALSE_KEY("validation.false"),
    IPV4("{0} must be a valid IPv4 address"),
    IPV4_KEY("validation.ipv4"),
    IPV6("{0} must be a valid IPv6 address"),
    IPV6_KEY("validation.ipv6"),
    MATCH("{0} must match {1}"),
    MATCH_KEY("validation.match"),
    MATCH_VALUES("The values of {0} is not valid"),
    MATCH_VALUES_KEY("validation.matchvalues"),
    MAX_LENGTH("{0} must be a value with a max length of {1}"),
    MAX_LENGTH_KEY("validation.max.length"),
    MAX_VALUE("{0} must be a value not greater than {1}"),
    MAX_VALUE_KEY("validation.max.value"),
    MIN_LENGTH("{0} must be a value with a min length of {1}"),
    MIN_LENGTH_KEY("validation.min.length"),
    MIN_VALUE("{0} must be a value not less thatn {1}"),
    MIN_VALUE_KEY("validation.min.value"),
    NOTNULL("{0} must be a value not that is null"),
    NOTNULL_KEY("validation.notnull"),
    NULL("{0} must be a value that is null"),
    NULL_KEY("validation.null"),
    NUMERIC("{0} must be a numeric value"),
    NUMERIC_KEY("validation.numeric"),
    RANGE_LENGTH("{0} must be a length between {1} and {2}"),
    RANGE_LENGTH_KEY("validation.range.length"),
    RANGE_VALUE("{0} must be value between {1} and {2}"),
    RANGE_VALUE_KEY("validation.range.value"),    
    REGEX("{0} is an invalid value"),
    REGEX_KEY("validation.regex"),
    REQUIRED("{0} is a required value"),
    REQUIRED_KEY("validation.required"),
    TRUE("{0} must be true"),
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