package io.mangoo.routing.bindings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mangoo.enums.Required;
import io.mangoo.models.Source;
import io.mangoo.utils.MangooUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.InputStream;
import java.util.*;

/**
 *
 * @author svenkubiak
 *
 */
public class Form extends Validator {
    private static final long serialVersionUID = -5815141142864033904L;
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
        Objects.requireNonNull(key, Required.KEY.toString());

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
        Objects.requireNonNull(key, Required.KEY.toString());

        String value = values.get(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        return "";
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

        String value = values.get(key);
        if (StringUtils.isNotBlank(value)) {
            switch (value) {
                case "1", "true" -> {
                    return Optional.of(Boolean.TRUE);
                }
                case "0", "false" -> {
                    return Optional.of(Boolean.FALSE);
                }
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
        Objects.requireNonNull(key, Required.KEY.toString());

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
        Objects.requireNonNull(key, Required.KEY.toString());

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
            return Optional.of(files.get(0));
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
        Objects.requireNonNull(key, Required.KEY.toString());

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
        Objects.requireNonNull(key, Required.KEY.toString());
        
        return valueMap.get(key);
    }
    
    /**
     * Checks if the form values are to put in the flash scope
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

        Optional s = this.toObject(Source.class);
        Source f = (Source) s.get();
    }

    /**
     * Converts the current form into a given object class
     *
     * @param clazz The class to convert to
     * @param <T> Ignored
     *
     * @return The converted object or null if conversion fails
     */
    public <T> T toObject(Class clazz) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());

        ObjectMapper mapper = new ObjectMapper();
        Object object = null;
        try {
            object = mapper.readValue(mapper.writeValueAsString(values), clazz);
        } catch (JsonProcessingException e) {
            //Intentionally left blank
        }

        return (T) object;
    }
    
    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
}