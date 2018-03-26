package io.mangoo.routing.bindings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import io.mangoo.enums.Required;

/**
 *
 * @author svenkubiak
 *
 */
public class Form extends Validator {
    private static final long serialVersionUID = -5815141142864033904L;
    private final List<File> files = new ArrayList<>();
    private final Map<String, List<String>> valueMap = new HashMap<>();
    private boolean submitted;
    private boolean flash;
    
    public Form() {
        //Empty constructor for google guice
    }
    
    /**
     * Retrieves an optional string value corresponding to the name of the form element
     *
     * @param key The name of the form element
     * @return Optional of String
     */
    public Optional<String> getString(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());

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
        Objects.requireNonNull(key, Required.KEY.toString());

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
            } else {
                // Ignore anything else
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
        Objects.requireNonNull(key, Required.KEY.toString());

        String value = this.values.get(key);
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
        Objects.requireNonNull(key, Required.KEY.toString());

        String value = this.values.get(key);
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
        Objects.requireNonNull(key, Required.KEY.toString());

        String value = this.values.get(key);
        if (StringUtils.isNotBlank(value) && NumberUtils.isCreatable(value)) {
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
        return new ArrayList<>(this.files);
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
 
    /**
     * Adds the form values to the flash scope
     */
    public void keep() {
        this.flash = true;
    }
    
    /**
     * Adds an additional item to the value list
     * @param key The name of the form element
     * @param value The value to store
     */
    public void addValueList(String key, String value) {
        Objects.requireNonNull(key, Required.KEY.toString());

        if (!valueMap.containsKey(key)) {
            List<String> values = new ArrayList<>();
            values.add(value);
            
            valueMap.put(key, values);
        } else {
            List<String> values = valueMap.get(key);
            values.add(value);
            
            valueMap.put(key, values);
        }
    }
    
    /**
     * Retrieves the value list for a given key
     * 
     * @param key The name of the form element
     * @return A value list with elements
     */
    public List<String> getValueList(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        
        return this.valueMap.get(key);
    }
    
    /**
     * Checks if the form values are to put in the flash scope
     * @return True if form values should be put into flash scope, false otherwise
     */
    public boolean flashify() {
        return this.flash;
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
}