package io.mangoo.routing.bindings;

import io.mangoo.constants.NotNull;
import io.mangoo.utils.MangooUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.InputStream;
import java.io.Serial;
import java.util.*;

public class Form extends Validator {
    @Serial
    private static final long serialVersionUID = 2228639200039277653L;
    private transient List<InputStream> files = new ArrayList<>();
    private Map<String, List<String>> valueMap = new HashMap<>();
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
        Objects.requireNonNull(key, NotNull.KEY);

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
        Objects.requireNonNull(key, NotNull.KEY);

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
    public Optional<Boolean> getBoolean(String key) {
        Objects.requireNonNull(key, NotNull.KEY);

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
        Objects.requireNonNull(key, NotNull.KEY);

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
        Objects.requireNonNull(key, NotNull.KEY);

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
        Objects.requireNonNull(key, NotNull.KEY);

        String value = values.get(key);
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
    public List<InputStream> getFiles() {
        return new ArrayList<>(files);
    }

    /**
     * Retrieves a single file of the form. If the form
     * has multiple files, the first will be returned
     *
     * @return File or null if no file is present
     */
    public Optional<InputStream> getFile() {
        if (!files.isEmpty()) {
            return Optional.of(files.getFirst());
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
    public void addFile(InputStream inputStream) {
        files.add(inputStream);
    }
 
    /**
     * Adds the form values to the flash scope
     */
    public void keep() {
        keep = true;
    }
    
    /**
     * Adds an item to the value list
     * @param key The name of the form element
     * @param value The value to store
     */
    public void addValueList(String key, String value) {
        Objects.requireNonNull(key, NotNull.KEY);

        List<String> values;
        if (!valueMap.containsKey(key)) {
            values = new ArrayList<>();
        } else {
            values = valueMap.get(key);
        }

        values.add(value);
        valueMap.put(key, values);
    }
    
    /**
     * Retrieves the value list for a given key
     * 
     * @param key The name of the form element
     * @return A value list with elements
     */
    public List<String> getValueList(String key) {
        Objects.requireNonNull(key, NotNull.KEY);
        
        return valueMap.get(key);
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
            files.forEach(MangooUtils::closeQuietly);            
        }
        valueMap = new HashMap<>();
    }
    
    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
}