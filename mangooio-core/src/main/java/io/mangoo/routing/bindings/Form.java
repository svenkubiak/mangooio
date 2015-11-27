package io.mangoo.routing.bindings;

import com.google.inject.Inject;
import io.mangoo.interfaces.MangooValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.*;

/**
 *
 * @author svenkubiak
 *
 */
public class Form implements MangooValidator {
    private static final String KEY_ERROR = "Key can not be null";
    private boolean submitted;
    private final List<File> files = new ArrayList<>();
    private final Map<String, String> values = new HashMap<>();
    private final Validator validator;

    @Inject
    public Form (Validator validator) {
        Objects.requireNonNull(validator, "Validator can not be null");

        this.validator = validator;
    }

    @Override
    public Validator validation() {
        return this.validator;
    }

    @Override
    public String getError(String fieldName) {
        return this.validator.hasError(fieldName) ? this.validator.getError(fieldName) : "";
    }

    /**
     * Retrieves a form value corresponding to the name of the form element
     *
     * @param key The name of the form element
     * @return The value of the form or null if not present
     */
    public String get(String key) {
        Objects.requireNonNull(key, KEY_ERROR);

        return this.values.get(key);
    }

    /**
     * Retrieves an optional string value corresponding to the name of the form element
     *
     * @param key The name of the form element
     * @return Optional of String
     */
    public Optional<String> getString(String key) {
        Objects.requireNonNull(key, KEY_ERROR);

        String value = this.values.get(key);
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }

        return Optional.empty();
    }

    /**
     * Retrieves an optional boolean value corresponding to the name of the form element
     *
     * 0 maps to false
     * 1 maps to true
     * "true" maps to true
     * "false" maps to false
     *
     * @param key The name of the form element
     * @return Optional of Boolean
     */
    public Optional<Boolean> getBoolean(String key) {
        Objects.requireNonNull(key, KEY_ERROR);

        String value = this.values.get(key);
        if (StringUtils.isNotBlank(value)) {
            if (("1").equals(value)) {
                return Optional.of(Boolean.TRUE);
            } else if (("true").equals(value)) {
                return Optional.of(Boolean.TRUE);
            } else if (("false").equals(value)) {
                return Optional.of(Boolean.FALSE);
            } else if (("0").equals(value)) {
                return Optional.of(Boolean.FALSE);
            }
        }

        return Optional.empty();
    }

    /**
     * Retrieves an optional integer value corresponding to the name of the form element
     *
     * @param key The name of the form element
     * @return Optional of Integer
     */
    public Optional<Integer> getInteger(String key) {
        Objects.requireNonNull(key, KEY_ERROR);

        String value = this.values.get(key);
        if (StringUtils.isNotBlank(value) && NumberUtils.isNumber(value)) {
            return Optional.of(Integer.valueOf(value));
        }

        return Optional.empty();
    }

    /**
     * Retrieves an optional double value corresponding to the name of the form element
     *
     * @param key The name of the form element
     * @return Optional of Double
     */
    public Optional<Double> getDouble(String key) {
        Objects.requireNonNull(key, KEY_ERROR);

        String value = this.values.get(key);
        if (StringUtils.isNotBlank(value) && NumberUtils.isNumber(value)) {
            return Optional.of(Double.valueOf(value));
        }

        return Optional.empty();
    }

    /**
     * Retrieves an optional float value corresponding to the name of the form element
     *
     * @param key The name of the form element
     * @return Optional of Float
     */
    public Optional<Float> getFloat(String key) {
        Objects.requireNonNull(key, KEY_ERROR);

        String value = this.values.get(key);
        if (StringUtils.isNotBlank(value) && NumberUtils.isNumber(value)) {
            return Optional.of(Float.valueOf(value));
        }

        return Optional.empty();
    }

    /**
     * Retrieves all attachment files of the form
     *
     * @return List of files or an empty list
     */
    public List<File> getFiles() {
        return this.files;
    }

    /**
     * Retrieves a single file of the form. If the the form
     * has multiple files, the first will be returned
     *
     * @return File or null if no file is present
     */
    public Optional<File> getFile() {
        if (!this.files.isEmpty()) {
            return Optional.of(this.files.get(0));
        }

        return Optional.empty();
    }

    /**
     * Retrieves all form submitted values where the key of the map
     * corresponds to the name of the form element and the value is
     * the value of the form element
     *
     * @return Map with Key-Value elements or empty map
     */
    public Map<String, String> getValues() {
        return this.values;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public void addValue(String key, String value) {
        this.values.put(key, value);
        this.validator.add(key, value);
    }
}