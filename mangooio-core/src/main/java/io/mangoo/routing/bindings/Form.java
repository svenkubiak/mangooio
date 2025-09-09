package io.mangoo.routing.bindings;

import io.mangoo.constants.Required;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Form extends Validator {
    @Serial
    private static final long serialVersionUID = 2228639200039277653L;
    private static final Logger LOG = LogManager.getLogger(Form.class);
    private boolean submitted;
    private boolean keep;
    
    public Form() {
        //Empty constructor for Google guice
    }
    
    /**
     * Retrieves an optional string value corresponding to the name of the form element
     *
     * @param key The name of the form element
     * @return Optional of String
     */
    public Optional<String> getString(String key) {
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }

        return Optional.empty();
    }
    
    /**
     * Retrieves a string value corresponding to the name of the form element
     *
     * @param key The name of the form element
     * @return String with the value of the form element or an empty value if blank
     */
    public String getValue(String key) {
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        return "";
    }

    /**
     * Retrieves an optional boolean value corresponding to the name of the form element
     * <p></p>
     * 0 maps to false
     * 1 maps to true
     * "true" maps to true
     * "false" maps to false
     *
     * @param key The name of the form element
     * @return Optional of Boolean
     */
    @SuppressWarnings("fb-contrib:BL_BURYING_LOGIC")
    public Optional<Boolean> getBoolean(String key) {
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isNotBlank(value)) {
            return switch (value) {
                case "1", "true" -> Optional.of(Boolean.TRUE);
                case "0", "false" -> Optional.of(Boolean.FALSE);
                default -> Optional.empty();
            };
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
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isNotBlank(value) && NumberUtils.isCreatable(value)) {
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
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isNotBlank(value) && NumberUtils.isCreatable(value)) {
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
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isNotBlank(value) && NumberUtils.isCreatable(value)) {
            return Optional.of(Float.valueOf(value));
        }

        return Optional.empty();
    }

    /**
     * Retrieves a single file of the form. If the form
     * has multiple files, the first will be returned
     *
     * @return File or null if no file is present
     */
    public Optional<byte[]> getFile(String key) {
        Objects.requireNonNull(key, Required.KEY);
        if (!files.isEmpty()) {
            return Optional.of(files.get(key));
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
        return values;
    }
    
    /**
     * Adds a file as an InputStream to the form
     *  
     * @param inputStream The InputStream to add
     */
    public void addFile(String key, InputStream inputStream) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(inputStream, Required.INPUT_STREAM);

        try {
            files.put(key, inputStream.readAllBytes());
        } catch (IOException e) {
            LOG.error("Failed to read InputStream for file upload", e);
        }
    }
 
    /**
     * Adds the form values to the flash scope
     */
    public void keep() {
        keep = true;
    }

    /**
     * Checks if the form values are to put in the flash scope
     * 
     * @return True if form values should be put into flash scope, false otherwise
     */
    public boolean isKept() {
        return keep;
    }
    
    /**
     * Discards the complete form
     */
    public void discard() {
        if (files != null) {
            files.clear();
            files = new HashMap<>();
            values.clear();
            values = new HashMap<>();
        }
    }
    
    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
}