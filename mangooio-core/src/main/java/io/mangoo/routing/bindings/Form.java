package io.mangoo.routing.bindings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;

import com.google.inject.Inject;

import io.mangoo.enums.Key;
import io.mangoo.i18n.Messages;

/**
 *
 * @author svenkubiak
 *
 */
public class Form {
    private boolean submitted;
    private List<File> files = new ArrayList<File>();
    private Map<String, String> values = new HashMap<String, String>();
    private Map<String, String> errors = new HashMap<String, String>();

    @Inject
    private Messages messages;

    public void add(String key, String value) {
        this.values.put(key, value);
    }

    public String get(String key) {
        return this.values.get(key);
    }

    public List<File> getFiles() {
        return this.files;
    }

    public File getFile() {
        File file = null;
        if (!this.files.isEmpty()) {
            file = this.files.get(0);
        }

        return file;
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    /**
     * Checks if a give field has a validation error
     *
     * @param fieldName The field to check
     * @return True if the field has a validation error, false otherwise
     */
    public boolean hasError(String fieldName) {
        return this.errors.containsKey(fieldName);
    }

    /**
     * Retrieves the error message for a given field
     *
     * @param fieldName The field to check
     * @return The error message for the field, or an empty string if no error is found
     */
    public String getError(String fieldName) {
        return hasError(fieldName) ? this.errors.get(fieldName) : "";
    }

    public String getValue(String fieldName) {
        return this.values.get(fieldName);
    }

    /**
     * Validates a given field to be required
     *
     * @param fieldName The field to check
     */
    public void required(String fieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);

        if (StringUtils.isBlank(StringUtils.trimToNull(value))) {
            this.errors.put(fieldName, messages.get(Key.FORM_REQUIRED, fieldName));
        }
    }

    /**
     * Validates a given field to have a minimum length
     *
     * @param minLength The minimum length
     * @param fieldName The field to check
     */
    public void min(int minLength, String fieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);

        if (value.length() < minLength) {
            this.errors.put(fieldName, messages.get(Key.FORM_MIN, fieldName, minLength));
        }
    }

    /**
     * Validates a given field to have a maximum length
     *
     * @param maxLength The maximum length
     * @param fieldName The field to check
     */
    public void max(int maxLength, String fieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);

        if (value.length() > maxLength) {
            this.errors.put(fieldName, messages.get(Key.FORM_MAX, fieldName, maxLength));
        }
    }

    /**
     * Validates to fields to exactly (case-sensitive) match
     *
     * @param fieldName The field to check
     * @param anotherFieldName The field to check against
     */
    public void exactMatch(String fieldName, String anotherFieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);
        String anotherValue = (get(anotherFieldName) == null) ? "" : get(anotherFieldName);

        if ( (StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue)) || !value.equals(anotherValue)) {
            this.errors.put(fieldName, messages.get(Key.FORM_EXACT_MATCH, fieldName, anotherFieldName));
        }
    }

    /**
     * Validates to fields to (case-insensitive) match
     *
     * @param fieldName The field to check
     * @param anotherFieldName The field to check against
     */
    public void match(String fieldName, String anotherFieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);
        String anotherValue = (get(anotherFieldName) == null) ? "" : get(anotherFieldName);

        if ((StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue)) || !value.equalsIgnoreCase(anotherValue)) {
            this.errors.put(fieldName, messages.get(Key.FORM_MATCH, fieldName, anotherFieldName));
        }
    }

    /**
     * Validates a field to be a valid email address
     *
     * @param fieldName The field to check
     */
    public void email(String fieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);

        if (!EmailValidator.getInstance().isValid(value)) {
            this.errors.put(fieldName, messages.get(Key.FORM_EMAIL, fieldName));
        }
    }

    /**
     * Validates a field to be a valid IPv4 address
     *
     * @param fieldName The field to check
     */
    public void ipv4(String fieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);

        if (!InetAddressValidator.getInstance().isValidInet4Address(value)) {
            this.errors.put(fieldName, messages.get(Key.FORM_IPV4, fieldName));
        }
    }

    /**
     * Validates a field to be a valid IPv6 address
     *
     * @param fieldName The field to check
     */
    public void ipv6(String fieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);

        if (!InetAddressValidator.getInstance().isValidInet6Address(value)) {
            this.errors.put(fieldName, messages.get(Key.FORM_IPV6, fieldName));
        }
    }

    /**
     * Validates a field to be in a certain range
     *
     * @param minLength The minimum length
     * @param maxLength The maximum length
     * @param fieldName The field to check
     */
    public void range(int minLength, int maxLength, String fieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);

        if (value.length() < minLength || value.length() > maxLength) {
            this.errors.put(fieldName, messages.get(Key.FORM_RANGE, fieldName, minLength, maxLength));
        }
    }

    /**
     * Validates field to be a valid URL
     *
     * @param fieldName The field to check
     */
    public void url(String fieldName) {
        String value = (get(fieldName) == null) ? "" : get(fieldName);

        if (!UrlValidator.getInstance().isValid(value)) {
            this.errors.put(fieldName, messages.get(Key.FORM_URL, fieldName));
        }
    }

    /**
     * Checks if any field in the validation has an error
     *
     * @return True if at least one field has an error, false otherwise
     */
    public boolean hasErrors() {
        return this.submitted && this.errors.size() > 0;
    }

    public Map<String, String> getValues() {
        return this.values;
    }

    public boolean hasContent() {
        return this.errors.size() > 0 && this.values.size() > 0;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
}