package io.mangoo.routing.bindings;

import com.google.re2j.Pattern;
import io.mangoo.constants.NotNull;
import io.mangoo.constants.Validation;
import io.mangoo.core.Application;
import io.mangoo.i18n.Messages;
import io.mangoo.utils.FileUtils;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class Validator implements Serializable {
    @Serial
    private static final long serialVersionUID = -714400230978999709L;
    private final Map<String, String> errors = new HashMap<>();
    private final Messages messages;
    protected Map<String, String> values = new HashMap<>(); // NOSONAR Intentionally not transient
    protected Map<String, InputStream> files = new HashMap<>(); // NOSONAR Intentionally not transient

    @Inject
    public Validator(Messages messages) {
        this.messages = Objects.requireNonNull(messages, NotNull.MESSAGES);
    }

    public Validator() {
        this.messages = Application.getInstance(Messages.class);
    }

    /**
     * Validates a given file upload to have one of the allowed mime types
     *
     * @param name The name of the field
     * @param message A custom error message instead of the default one
     * @param allowedMimeTypes A list of allowed MimeTypes
     */
    public void expectFileMimeType(String name, String message, String... allowedMimeTypes) {
        Objects.requireNonNull(name, NotNull.NAME);
        Objects.requireNonNull(allowedMimeTypes, NotNull.ALLOWED_MIME_TYPES);

        InputStream inputStream = files.get(name);

        if (inputStream != null) {
            try {
                if (!inputStream.markSupported()) {
                    inputStream = new BufferedInputStream(inputStream);
                }

                inputStream.mark(Integer.MAX_VALUE);
                String detectedType = FileUtils.getMimeType(inputStream);
                inputStream.reset();

                for (String type : allowedMimeTypes) {
                    if (type.equalsIgnoreCase(detectedType)) {
                        addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MIME_TYPE_KEY, name)));
                    }
                }
            } catch (Exception e) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MIME_TYPE_KEY, name)));
            }
        } else {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MIME_TYPE_KEY, name)));
        }
    }

    /**
     * Validates a given file upload to have one of the allowed mime types
     *
     * @param name The name of the field
     * @param allowedMimeTypes A list of allowed MimeTypes
     */
    public void expectFileMimeType(String name, String... allowedMimeTypes) {
        expectFileMimeType(name, null, allowedMimeTypes);
    }

    /**
     * Validates a given file upload to have a max file size in bytes
     *
     * @param name The field name
     * @param maxFileSizeBytes The maximum file size in bytes
     * @param message A custom error message instead of the default one
     */
    public void expectFileMaxSize(String name, long maxFileSizeBytes, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        InputStream inputStream = files.get(name);

        if (inputStream != null) {
            try {
                byte[] chunk = new byte[8192];
                long totalRead = 0;

                int bytesRead;
                while ((bytesRead = inputStream.read(chunk)) != -1) {
                    totalRead += bytesRead;
                    if (totalRead > maxFileSizeBytes) {
                        addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.FILE_SIZE_KEY, name)));
                    }
                }
            } catch (Exception e) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.FILE_SIZE_KEY, name)));
            }
        } else {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.FILE_SIZE_KEY, name)));
        }
    }

    /**
     * Validates a given file upload to have a max file size in bytes
     *
     * @param name The field name
     * @param maxFileSizeBytes The maximum file size in bytes
     */
    public void expectFileMaxSize(String name, long maxFileSizeBytes) {
        expectFileMaxSize(name, maxFileSizeBytes, null);
    }

    /**
     * Checks if a give field has a validation error
     *
     * @param name The field to check
     * @return True if the field has a validation error, false otherwise
     */
    public boolean hasError(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        return errors.containsKey(name);
    }

    /**
     * Retrieves the error message for a given field
     *
     * @param name The field to check
     * @return The error message for the field, or an empty string if no error is found
     */
    public String getError(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        return hasError(name) ? errors.get(name) : Strings.EMPTY;
    }

    /**
     * Validates a given field to be present with a value
     *
     * @param name The field to check
     */
    public void expectValue(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectValue(name, null);
    }
    
    /**
     * Validates a given field to be present with a value
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectValue(String name, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (StringUtils.isBlank(StringUtils.trimToNull(value))) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.REQUIRED_KEY, name)));
        }
    }

    /**
     * Validates a given field to have a minimum value
     *
     * @param name The field to check
     * @param minValue The minimum value
     */
    public void expectMinValue(String name, double minValue) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectMinValue(name, minValue, null);
    }
    
    /**
     * Validates a given field to have a minimum value
     *
     * @param name The field to check
     * @param minValue The minimum value
     * @param message A custom error message instead of the default one
     */
    public void expectMinValue(String name, double minValue, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (StringUtils.isNumeric(value)) {
            if (Double.parseDouble(value) < minValue) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MIN_VALUE_KEY, name, minValue)));
            }
        } else {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MIN_VALUE_KEY, name, minValue)));
        }
    }

    /**
     * Validates a given field to have a minimum length
     *
     * @param name The field to check
     * @param minLength The minimum length
     */
    public void expectMinLength(String name, double minLength) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectMinLength(name, minLength, null);
    }
    
    /**
     * Validates a given field to have a minimum length
     *
     * @param name The field to check
     * @param minLength The minimum length
     * @param message A custom error message instead of the default one
     */
    public void expectMinLength(String name, double minLength, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (value.length() < minLength) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MIN_LENGTH_KEY, name, minLength)));
        }
    }
    
    /**
     * Validates a given field to have a maximum value
     *
     * @param maxValue The maximum value
     * @param name The field to check
     */
    public void expectMaxValue(String name, double maxValue) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectMaxValue(name, maxValue, null);
    }
    
    /**
     * Validates a given field to have a maximum length
     *
     * @param maxLength The maximum length
     * @param name The field to check
     */
    public void expectMaxLength(String name, double maxLength) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectMaxLength(name, maxLength, null);
    }
    
    /**
     * Validates that a given field has a numeric value
     *
     * @param name The field to check
     *
     */
    public void expectNumeric(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectNumeric(name, null);
    }

    /**
     * Validates that a given field has a numeric value
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectNumeric(String name, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!StringUtils.isNumeric(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.NUMERIC_KEY, name)));
        }
    }
    
    /**
     * Validates a given field to have a maximum length
     *
     * @param name The field to check
     * @param maxLength The maximum length
     * @param message A custom error message instead of the default one
     */
    public void expectMaxLength(String name, double maxLength, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (value.length() > maxLength) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MAX_LENGTH_KEY, name, maxLength)));
        }
    }

    /**
     * Validates a given field to have a maximum value
     *
     * @param name The field to check
     * @param maxValue The maximum value
     * @param message A custom error message instead of the default one
     */
    public void expectMaxValue(String name, double maxValue, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (StringUtils.isNumeric(value)) {
            if (Double.parseDouble(value) > maxValue) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MAX_VALUE_KEY, name, maxValue)));
            }
        } else {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MAX_VALUE_KEY, name, maxValue)));
        }
    }

    /**
     * Validates two fields to exactly (case-sensitive) match
     *
     * @param name The field to check
     * @param anotherName The other field to check against
     */
    public void expectExactMatch(String name, String anotherName) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectExactMatch(name, anotherName, null);
    }

    /**
     * Validates two fields to exactly (case-sensitive) match
     *
     * @param name The field to check
     * @param anotherName The other field to check against
     * @param message A custom error message instead of the default one
     */
    public void expectExactMatch(String name, String anotherName, String message) {
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);
        String anotherValue = Optional.ofNullable(get(anotherName)).orElse(Strings.EMPTY);

        if (( StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue) ) || ( StringUtils.isNotBlank(value) && !value.equals(anotherValue) )) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.EXACT_MATCH_KEY, name, anotherName)));
        }
    }

    /**
     * Validates two fields to (case-insensitive) match
     *
     * @param name The field to check
     * @param anotherName The field to check against
     */
    public void expectMatch(String name, String anotherName) {
        expectMatch(name, anotherName, messages.get(Validation.MATCH_KEY, name, anotherName));
    }
    
    /**
     * Validates two fields to (case-insensitive) match
     *
     * @param name The field to check
     * @param anotherName The field to check against
     * @param message A custom error message instead of the default one
     */
    public void expectMatch(String name, String anotherName, String message) {
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);
        String anotherValue = Optional.ofNullable(get(anotherName)).orElse(Strings.EMPTY);

        if (( StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue) ) || ( StringUtils.isNotBlank(value) && !value.equalsIgnoreCase(anotherValue)  )) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MATCH_KEY, name, anotherName)));
        }
    }

    /**
     * Validates a list of given values to (case-sensitive) match
     *
     * @param name The field to check
     * @param values A list of given values to check against
     */
    public void expectMatch(String name, List<String> values) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectMatch(name, messages.get(Validation.MATCH_VALUES_KEY, name), values);
    }

    /**
     * Validates a list of value to (case-sensitive) match
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     * @param values A list of given values to check against
     */
    public void expectMatch(String name, String message, List<String> values) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!(values).contains(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.MATCH_VALUES_KEY, name)));
        }
    }

    /**
     * Validates a field to be a valid email address
     *
     * @param name The field to check
     */
    public void expectEmail(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectEmail(name, null);
    }

    /**
     * Validates a field to be a valid email address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectEmail(String name, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!EmailValidator.getInstance().isValid(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.EMAIL_KEY, name)));
        }
    }

    /**
     * Validates a field to be a valid IPv4 address
     *
     * @param name The field to check
     */
    public void expectIpv4(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectIpv4(name, null);
    }

    /**
     * Validates a field to be a valid IPv4 address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectIpv4(String name, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!InetAddressValidator.getInstance().isValidInet4Address(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.IPV4_KEY, name)));
        }
    }
    
    /**
     * Validates a field to be a valid Domain name
     *
     * @param name The field to check
     */
    public void expectDomainName(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectDomainName(name, null);
    }

    /**
     * Validates a field to be a valid Domain name
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectDomainName(String name, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!DomainValidator.getInstance().isValid(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.DOMAIN_NAME_KEY, name)));
        }
    }

    /**
     * Validates a field to be a valid IPv6 address
     *
     * @param name The field to check
     */
    public void expectIpv6(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectIpv6(name, null);
    }

    /**
     * Validates a field to be a valid IPv6 address
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectIpv6(String name, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!InetAddressValidator.getInstance().isValidInet6Address(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.IPV6_KEY, name)));
        }
    }
    
    /**
     * Validates a field to be in a certain range length
     *
     * @param name The field to check
     * @param minLength The minimum length
     * @param maxLength The maximum length
     */
    public void expectRangeLength(String name, int minLength, int maxLength) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectRangeLength(name, minLength, maxLength, null);
    }
    
    /**
     * Validates a field to be in a certain range value
     *
     * @param name The field to check
     * @param minValue The minimum value
     * @param maxValue The maximum value
     */
    public void expectRangeValue(String name, int minValue, int maxValue) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectRangeValue(name, minValue, maxValue, null);
    } 
    
    /**
     * Validates a field to be in a certain range value
     * 
     * @param name The field to check
     * @param minValue The minimum value
     * @param maxValue The maximum value
     * @param message A custom error message instead of the default one
     */
    public void expectRangeValue(String name, int minValue, int maxValue, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (StringUtils.isNumeric(value)) {
            var doubleValue = Double.parseDouble(value);
            if (doubleValue < minValue || doubleValue > maxValue) {
                addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.RANGE_VALUE_KEY, name, minValue, maxValue)));
            }
        } else {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.RANGE_VALUE_KEY, name, minValue, maxValue)));
        }
    }
    
    /**
     * Validates a field to be in a certain range length
     * 
     * @param name The field to check
     * @param minLength The minimum length
     * @param maxLength The maximum length
     * @param message A custom error message instead of the default one
     */
    public void expectRangeLength(String name, int minLength, int maxLength, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (value.length() < minLength || value.length() > maxLength) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.RANGE_LENGTH_KEY, name, minLength, maxLength)));
        }
    }

    /**
     * Validates a field by a given regular expression pattern
     * <p>
     * It is required to pass a pre-compiled pattern, e.g.
     * Pattern pattern = Pattern.compile("[a-Z,0-9]")
     *
     * @param name The field to check
     * @param pattern The pre-compiled pattern
     */
    public void expectRegex(String name, Pattern pattern) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectRegex(name, pattern, null);
    }

    /**
     * Validates a field by a given regular expression pattern
     * <p>
     * It is required to pass a pre-compiled pattern, e.g.
     * Pattern pattern = Pattern.compile("[a-Z,0-9]")
     *
     * @param pattern The pre-compiled pattern
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectRegex(String name, Pattern pattern, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!pattern.matcher(value).matches()) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.REGEX_KEY, name)));
        }
    }

    /**
     * Validates field to be a valid URL
     *
     * @param name The field to check
     */
    public void expectUrl(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectUrl(name, null);
    }

    /**
     * Validates field to be a valid URL
     *
     * @param name The field to check
     * @param message A custom error message instead of the default one
     */
    public void expectUrl(String name, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!UrlValidator.getInstance().isValid(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.URL_KEY, name)));
        }
    }

    /**
     * Validates a given value to be true
     *
     * @param name The name of the field to display the error message
     * @param value The value to check
     * @param message A custom error message instead of the default one
     */
    public void expectTrue(String name, boolean value, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        if (!value) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.TRUE_KEY, name)));
        }
    }
    
    /**
     * Validates a given value to be true
     *
     * @param name The name of the field to display the error message
     * @param value The value to check
     */
    public void expectTrue(String name, boolean value) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectTrue(name, value, null);
    }
    
    /**
     * Validates a given value to be false
     *
     * @param name The name of the field to display the error message
     * @param value The value to check
     * @param message A custom error message instead of the default one
     */
    public void expectFalse(String name, boolean value, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        if (value) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.FALSE_KEY, name)));
        }
    }
    
    /**
     * Validates a given value to be false
     *
     * @param name The name of the field to display the error message
     * @param value The value to check
     */
    public void expectFalse(String name, boolean value) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectFalse(name, value, null);
    }
    
    /**
     * Validates a given object to be not null
     *
     * @param name The name of the field to display the error message
     * @param object The object to check
     * @param message A custom error message instead of the default one
     */
    public void expectNotNull(String name, Object object, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        if (object == null) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.NOTNULL_KEY, name)));
        }
    }
    
    /**
     * Validates a given object to be not null
     *
     * @param name The name of the field to display the error message
     * @param object The object to check
     */
    public void expectNotNull(String name, Object object) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectNotNull(name, object, null);
    }
    
    /**
     * Validates a given object to be null
     *
     * @param name The name of the field to display the error message
     * @param object The object to check
     * @param message A custom error message instead of the default one 
     */
    public void expectNull(String name, Object object, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        if (object != null) {
            addError(name, Optional.ofNullable(message).orElse(messages.get(Validation.NULL_KEY, name)));
        }
    }
    
    /**
     * Validates a given object to be null
     *
     * @param name The name of the field to display the error message
     * @param object The object to check
     */
    public void expectNull(String name, Object object) {
        Objects.requireNonNull(name, NotNull.NAME);
        expectNull(name, object, messages.get(Validation.NULL_KEY, name));
    }
    
    /**
     * Checks if any field in the validation has an error
     *
     * @return True if at least one field has an error, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Retrieves a form value corresponding to the name of the form element
     *
     * @param name The name of the form element
     * @return The value of the form or null if not present
     */
    public String get(String name) {
        Objects.requireNonNull(name, NotNull.NAME);

        return values.get(name);
    }

    private void addError(String name, String message) {
        Objects.requireNonNull(name, NotNull.NAME);
        Objects.requireNonNull(message, NotNull.MESSAGE);

        errors.computeIfAbsent(name, k -> message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }
    
    public void addValue(String key, String value) {
        values.put(key, value);
    }
    
    public boolean isValid() {
        return !hasErrors();
    }
    
    public void invalidate() {
        errors.put(Strings.EMPTY, Strings.EMPTY);
    }
}