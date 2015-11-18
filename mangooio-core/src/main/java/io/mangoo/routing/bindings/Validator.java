package io.mangoo.routing.bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import io.mangoo.enums.Key;
import io.mangoo.i18n.Messages;

/**
 *
 * @author svenkubiak
 *
 */
public class Validator {
    private final Map<String, String> errors = new HashMap<>();
    private Map<String, String> values = new HashMap<>();
    private final Messages messages;

    @Inject
    public Validator(Messages messages) {
        Preconditions.checkNotNull(messages, "Messages can not be null");

        this.messages = messages;
    }

    /**
     * Checks if a give field has a validation error
     *
     * @param name The field to check
     * @return True if the field has a validation error, false otherwise
     */
    public boolean hasError(String name) {
        return this.errors.containsKey(name);
    }

    /**
     * Retrieves the error message for a given field
     *
     * @param name The field to check
     * @return The error message for the field, or an empty string if no error is found
     */
    public String getError(String name) {
        return hasError(name) ? this.errors.get(name) : "";
    }

    /**
     * Validates a given field to be required
     *
     * @param name The field to check
     */
    public void required(String name) {
        required(name, messages.get(Key.VALIDATION_REQUIRED, name));
    }

    /**
     * Validates a given field to be required
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void required(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (StringUtils.isBlank(StringUtils.trimToNull(value))) {
            this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_REQUIRED, name)));
        }
    }

    /**
     * Validates a given field to have a minimum length
     *
     * @param name The field to check
     * @param minLength The minimum length
     */
    public void min(String name, double minLength) {
        min(name, minLength, messages.get(Key.VALIDATION_MIN, name, minLength));
    }

    /**
     * Validates a given field to have a minimum length
     *
     * @param name The field to check
     * @param minLength The minimum length
     * @param message A custom error message instead of the default one
     */
    public void min(String name, double minLength, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (StringUtils.isNumeric(value)) {
            if (Double.valueOf(value) < minLength) {
                this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_MIN, name, minLength)));
            }
        } else {
            if (value.length() < minLength) {
                this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_MIN, name, minLength)));
            }
        }
    }

    /**
     * Validates a given field to have a maximum length
     *
     * @param maxLength The maximum length
     * @param name The field to check
     *
     */
    public void max(String name, double maxLength) {
        max(name, maxLength, messages.get(Key.VALIDATION_MAX, name, maxLength));
    }

    /**
     * Validates a given field to have a maximum length
     *
     * @param name The field to check
     * @param maxLength The maximum length
     * @param message A custom error message instead of the default one
     */
    public void max(String name, double maxLength, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (StringUtils.isNumeric(value)) {
            if (Double.valueOf(value) > maxLength) {
                this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_MAX, name, maxLength)));
            }
        } else {
            if (value.length() > maxLength) {
                this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_MAX, name, maxLength)));
            }
        }
    }

    /**
     * Validates to fields to exactly (case-sensitive) match
     *
     * @param name The field to check
     * @param anotherName The field to check against
     */
    public void exactMatch(String name, String anotherName) {
        exactMatch(name, anotherName, messages.get(Key.VALIDATION_EXACT_MATCH, name, anotherName));
    }

    /**
     * Validates to fields to exactly (case-sensitive) match
     *
     * @param name The field to check
     * @param anotherName The field to check against
     * @param message A custom error message instead of the default one
     */
    public void exactMatch(String name, String anotherName, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");
        String anotherValue = Optional.ofNullable(get(anotherName)).orElse("");

        if ( (StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue)) || !value.equals(anotherValue)) {
            this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_EXACT_MATCH, name, anotherName)));
        }
    }

    /**
     * Validates to fields to (case-insensitive) match
     *
     * @param name The field to check
     * @param anotherName The field to check against
     */
    public void match(String name, String anotherName) {
        match(name, anotherName, messages.get(Key.VALIDATION_MATCH, name, anotherName));
    }

    /**
     * Validates to fields to (case-insensitive) match
     *
     * @param name The field to check
     * @param anotherName The field to check against
     * @param message A custom error message instead of the default one
     */
    public void match(String name, String anotherName, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");
        String anotherValue = Optional.ofNullable(get(anotherName)).orElse("");

        if ((StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue)) || !value.equalsIgnoreCase(anotherValue)) {
            this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_MATCH, name, anotherName)));
        }
    }

    /**
     * Validates a field to be a valid email address
     *
     * @param name The field to check
     */
    public void email(String name) {
        email(name, messages.get(Key.VALIDATION_EMAIL, name));
    }

    /**
     * Validates a field to be a valid email address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void email(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!EmailValidator.getInstance().isValid(value)) {
            this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_EMAIL, name)));
        }
    }

    /**
     * Validates a field to be a valid IPv4 address
     *
     * @param name The field to check
     */
    public void ipv4(String name) {
        ipv4(name, messages.get(Key.VALIDATION_IPV4, name));
    }

    /**
     * Validates a field to be a valid IPv4 address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void ipv4(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!InetAddressValidator.getInstance().isValidInet4Address(value)) {
            this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_IPV4, name)));
        }
    }

    /**
     * Validates a field to be a valid IPv6 address
     *
     * @param name The field to check
     */
    public void ipv6(String name) {
        ipv6(name, messages.get(Key.VALIDATION_IPV6, name));
    }

    /**
     * Validates a field to be a valid IPv6 address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void ipv6(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!InetAddressValidator.getInstance().isValidInet6Address(value)) {
            this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_IPV6, name)));
        }
    }

    /**
     * Validates a field to be in a certain range
     *
     * @param name The field to check
     * @param minLength The minimum length
     * @param maxLength The maximum length
     */
    public void range(String name, int minLength, int maxLength) {
        range(name, minLength, maxLength, messages.get(Key.VALIDATION_RANGE, name, minLength, maxLength));
    }

    /**
     * Validates a field to be in a certain range
     *
     * @param name The field to check
     * @param minLength The minimum length
     * @param maxLength The maximum length
     * @param message A custom error message instead of the default one
     */
    public void range(String name, int minLength, int maxLength, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (StringUtils.isNumeric(value)) {
            double doubleValue = Double.parseDouble(value);
            if (doubleValue < minLength || doubleValue > maxLength) {
                this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_RANGE, name, minLength, maxLength)));
            }
        } else {
            if (value.length() < minLength || value.length() > maxLength) {
                this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_RANGE, name, minLength, maxLength)));
            }
        }
    }

    /**
     * Validates a field by a given regular expression pattern
     *
     * It is required to pass a pre-compiled pattern, e.g.
     * Pattern pattern = Pattern.compile("[a-Z,0-9]")
     *
     * @param name The field to check
     * @param pattern The pre-compiled pattern
     */
    public void regex(String name, Pattern pattern) {
        regex(name, pattern, messages.get(Key.VALIDATION_REGEX, name));
    }

    /**
     * Validates a field by a given regular expression pattern
     *
     * It is required to pass a pre-compiled pattern, e.g.
     * Pattern pattern = Pattern.compile("[a-Z,0-9]")
     *
     * @param pattern The pre-compiled pattern
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void regex(String name, Pattern pattern, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!pattern.matcher(value).matches()) {
            this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_REGEX, name)));
        }
    }

    /**
     * Validates field to be a valid URL
     *
     * @param name The field to check
     */
    public void url(String name) {
        url(name, messages.get(Key.VALIDATION_URL, name));
    }

    /**
     * Validates field to be a valid URL
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void url(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!UrlValidator.getInstance().isValid(value)) {
            this.errors.put(name, Optional.ofNullable(message).orElse(messages.get(Key.VALIDATION_URL, name)));
        }
    }

    /**
     * Checks if any field in the validation has an error
     *
     * @return True if at least one field has an error, false otherwise
     */
    public boolean hasErrors() {
        return this.errors.size() > 0;
    }

    public String get(String key) {
        return this.values.get(key);
    }

    public Map<String, String> getErrors() {
        return this.errors;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public void add(String key, String value) {
        this.values.put(key, value);
    }
}