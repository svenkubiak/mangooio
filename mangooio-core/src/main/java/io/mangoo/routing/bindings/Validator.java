package io.mangoo.routing.bindings;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;

import com.google.inject.Inject;

import io.mangoo.enums.Required;
import io.mangoo.enums.Validation;
import io.mangoo.i18n.Messages;

/**
 *
 * @author svenkubiak
 *
 */
public class Validator implements Serializable {
    private static final long serialVersionUID = -2467664448802191044L;
    private final Map<String, String> errors = new HashMap<>();
    protected Map<String, String> values = new HashMap<>(); //NOSONAR
    
    @Inject
    private Messages messages;

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
     * Validates a given field to be present with a value
     *
     * @param name The field to check
     */
    public void expectValue(String name) {
        expectValue(name, messages.get(Validation.REQUIRED_KEY.name(), name));
    }

    /**
     * Validates a given field to be present with a value
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectValue(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (StringUtils.isBlank(StringUtils.trimToNull(value))) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.REQUIRED_KEY.name(), name)));
        }
    }
    
    /**
     * Validates a given field to have a minimum length
     * 
     * @param name The field to check
     * @param minLength The minimum length
     */
    public void expectMin(String name, double minLength) {
        expectMin(name, minLength, messages.get(Validation.MIN_KEY.name(), name, minLength));
    }


    /**
     * Validates a given field to have a minimum length
     *
     * @param name The field to check
     * @param minLength The minimum length
     * @param message A custom error message instead of the default one
     */
    public void expectMin(String name, double minLength, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (StringUtils.isNumeric(value)) {
            if (Double.parseDouble(value) < minLength) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MIN_KEY.name(), name, minLength)));
            }
        } else {
            if (value.length() < minLength) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MIN_KEY.name(), name, minLength)));
            }
        }
    }

    /**
     * Validates a given field to have a maximum length
     *
     * @param maxLength The maximum length
     * @param name The field to check
     */
    public void expectMax(String name, double maxLength) {
        expectMax(name, maxLength, messages.get(Validation.MAX_KEY.name(), name, maxLength));
    }
    
    /**
     * Validates that a given field has a numeric value
     *
     * @param name The field to check
     *
     */
    public void expectNumeric(String name) {
        expectNumeric(name, messages.get(Validation.NUMERIC_KEY.name(), name));
    }
    
    /**
     * Validates that a given field has a numeric value
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectNumeric(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!StringUtils.isNumeric(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.NUMERIC_KEY.name(), name)));
        }
    }

    /**
     * Validates a given field to have a maximum length
     *
     * @param name The field to check
     * @param maxLength The maximum length
     * @param message A custom error message instead of the default one
     */
    public void expectMax(String name, double maxLength, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (StringUtils.isNumeric(value)) {
            if (Double.parseDouble(value) > maxLength) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MAX_KEY.name(), name, maxLength)));
            }
        } else {
            if (value.length() > maxLength) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MAX_KEY.name(), name, maxLength)));
            }
        }
    }

    /**
     * Validates to fields to exactly (case-sensitive) match
     *
     * @param name The field to check
     * @param anotherName The other field to check against
     */
    public void expectExactMatch(String name, String anotherName) {
        expectExactMatch(name, anotherName, messages.get(Validation.EXACT_MATCH_KEY.name(), name, anotherName));
    }

    /**
     * Validates to fields to exactly (case-sensitive) match
     *
     * @param name The field to check
     * @param anotherName The other field to check against
     * @param message A custom error message instead of the default one
     */
    public void expectExactMatch(String name, String anotherName, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");
        String anotherValue = Optional.ofNullable(get(anotherName)).orElse("");

        if (( StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue) ) || ( StringUtils.isNotBlank(value) && !value.equals(anotherValue) )) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.EXACT_MATCH_KEY.name(), name, anotherName)));
        } 
    }

    /**
     * Validates to fields to (case-insensitive) match
     *
     * @param name The field to check
     * @param anotherName The field to check against
     */
    public void expectMatch(String name, String anotherName) {
        expectMatch(name, anotherName, messages.get(Validation.MATCH_KEY.name(), name, anotherName));
    }

    /**
     * Validates to fields to (case-insensitive) match
     *
     * @param name The field to check
     * @param anotherName The field to check against
     * @param message A custom error message instead of the default one
     */
    public void expectMatch(String name, String anotherName, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");
        String anotherValue = Optional.ofNullable(get(anotherName)).orElse("");

        if (( StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue) ) || ( StringUtils.isNotBlank(value) && !value.equalsIgnoreCase(anotherValue)  )) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MATCH_KEY.name(), name, anotherName)));
        } 
    }
    
    /**
     * Validates to list of given values to (case-sensitive) match
     *
     * @param name The field to check
     * @param values A list of given values to check against
     */
    public void expectMatch(String name, List<String> values) {
        expectMatch(name, messages.get(Validation.MATCH_VALUES_KEY.name(), name), values);
    }

    /**
     * Validates to fields to (case-sensitive) match
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     * @param values A list of given values to check against
     */
    public void expectMatch(String name, String message, List<String> values) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!(values).contains(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MATCH_VALUES_KEY.name(), name)));
        }
    }

    /**
     * Validates a field to be a valid email address
     *
     * @param name The field to check
     */
    public void expectEmail(String name) {
        expectEmail(name, messages.get(Validation.EMAIL_KEY.name(), name));
    }

    /**
     * Validates a field to be a valid email address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectEmail(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!EmailValidator.getInstance().isValid(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.EMAIL_KEY.name(), name)));
        }
    }

    /**
     * Validates a field to be a valid IPv4 address
     *
     * @param name The field to check
     */
    public void expectIpv4(String name) {
        expectIpv4(name, messages.get(Validation.IPV4_KEY.name(), name));
    }

    /**
     * Validates a field to be a valid IPv4 address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectIpv4(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!InetAddressValidator.getInstance().isValidInet4Address(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.IPV4_KEY.name(), name)));
        }
    }
    
    /**
     * Validates a field to be a valid IPv4 address
     *
     * @param name The field to check
     */
    public void expectDomainName(String name) {
        expectDomainName(name, messages.get(Validation.IPV4_KEY.name(), name));
    }

    /**
     * Validates a field to be a valid IPv4 address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectDomainName(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!DomainValidator.getInstance().isValid(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.DOMAIN_NAME_KEY.name(), name)));
        }
    }

    /**
     * Validates a field to be a valid IPv6 address
     *
     * @param name The field to check
     */
    public void expectIpv6(String name) {
        expectIpv6(name, messages.get(Validation.IPV6_KEY.name(), name));
    }

    /**
     * Validates a field to be a valid IPv6 address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectIpv6(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!InetAddressValidator.getInstance().isValidInet6Address(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.IPV6_KEY.name(), name)));
        }
    }

    /**
     * Validates a field to be in a certain range
     *
     * @param name The field to check
     * @param minLength The minimum length
     * @param maxLength The maximum length
     */
    public void expectRange(String name, int minLength, int maxLength) {
        expectRange(name, minLength, maxLength, messages.get(Validation.RANGE_KEY.name(), name, minLength, maxLength));
    }
    
    /**
     * Validates a field to be in a certain range
     *
     * @param name The field to check
     * @param minLength The minimum length
     * @param maxLength The maximum length
     * @param message A custom error message instead of the default one
     */
    public void expectRange(String name, int minLength, int maxLength, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (StringUtils.isNumeric(value)) {
            double doubleValue = Double.parseDouble(value);
            if (doubleValue < minLength || doubleValue > maxLength) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.RANGE_KEY.name(), name, minLength, maxLength)));
            }
        } else {
            if (value.length() < minLength || value.length() > maxLength) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.RANGE_KEY.name(), name, minLength, maxLength)));
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
    public void expectRegex(String name, Pattern pattern) {
        expectRegex(name, pattern, messages.get(Validation.REGEX_KEY.name(), name));
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
    public void expectRegex(String name, Pattern pattern, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!pattern.matcher(value).matches()) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.REGEX_KEY.name(), name)));
        }
    }

    /**
     * Validates field to be a valid URL
     *
     * @param name The field to check
     */
    public void expectUrl(String name) {
        expectUrl(name, messages.get(Validation.URL_KEY.name(), name));
    }

    /**
     * Validates field to be a valid URL
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectUrl(String name, String message) {
        String value = Optional.ofNullable(get(name)).orElse("");

        if (!UrlValidator.getInstance().isValid(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.URL_KEY.name(), name)));
        }
    }
    
    /**
     * Validates a given value to be true
     *
     * @param value The value to check
     * @param name The name of the field to display the error message
     * @param message A custom error message instead of the default one
     */
    public void validateTrue(boolean value, String name, String message) {
        if (!value) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.TRUE_KEY.name(), name)));
        }
    }
    
    /**
     * Validates a given value to be true
     *
     * @param value The value to check
     * @param name The name of the field to display the error message
     */
    public void validateTrue(boolean value, String name) {
        validateTrue(value, name, messages.get(Validation.TRUE_KEY.name(), name));
    }
    
    /**
     * Validates a given value to be false
     *
     * @param value The value to check
     * @param name The name of the field to display the error message
     * @param message A custom error message instead of the default one
     */
    @SuppressWarnings("all")
    public void validateFalse(boolean value, String name, String message) {
        if (value) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.FALSE_KEY.name(), name)));
        }
    }
    
    /**
     * Validates a given value to be false
     *
     * @param value The value to check
     * @param name The name of the field to display the error message
     */
    public void validateFalse(boolean value, String name) {
        validateFalse(value, name, messages.get(Validation.FALSE_KEY.name(), name));
    }
    
    /**
     * Validates a given object to be not null
     *
     * @param object The object to check
     * @param name The name of the field to display the error message
     * @param message A custom error message instead of the default one
     */
    public void validateNotNull(Object object, String name, String message) {
        if (object == null) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.NOTNULL_KEY.name(), name)));
        }
    }
    
    /**
     * Validates a given object to be not null
     *
     * @param object The object to check
     * @param name The name of the field to display the error message
     */
    public void validateNotNull(Object object, String name) {
        validateNotNull(object, name, messages.get(Validation.NOTNULL_KEY.name(), name));
    }
    
    /**
     * Validates a given object to be null
     *
     * @param object The object to check
     * @param name The name of the field to display the error message
     * @param message A custom error message instead of the default one 
     */
    public void validateNull(Object object, String name, String message) {
        if (object != null) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.NULL_KEY.name(), name)));
        }
    }
    
    /**
     * Validates a given object to be null
     *
     * @param object The object to check
     * @param name The name of the field to display the error message
     */
    public void validateNull(Object object, String name) {
        validateNull(object, name, messages.get(Validation.NULL_KEY.name(), name));
    }

    /**
     * Checks if any field in the validation has an error
     *
     * @return True if at least one field has an error, false otherwise
     */
    public boolean hasErrors() {
        return this.errors.size() > 0;
    }

    /**
     * Retrieves a form value corresponding to the name of the form element
     *
     * @param key The name of the form element
     * @return The value of the form or null if not present
     */
    public String get(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());

        return this.values.get(key);
    }

    private void addError(String name, String message) {
        Objects.requireNonNull(name, Required.NAME.toString());
        Objects.requireNonNull(message, Required.MESSAGE.toString());
        
        if (!this.errors.containsKey(name)) {
            this.errors.put(name, message);            
        }
    }

    public Map<String, String> getErrors() {
        return this.errors;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }
    
    public void addValue(String key, String value) {
        this.values.put(key, value);
    }
    
    public boolean isValid() {
        return !hasErrors();
    }
}