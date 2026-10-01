package io.mangoo.routing.bindings;

import io.mangoo.constants.Required;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Flash {
    private static final Logger LOG = LogManager.getLogger(Flash.class);
    private static final Set<String> INVALID_CHARACTERS = Set.of("|", ":", "&", " ");
    private static final String ERROR = "error";
    private static final String WARNING = "warning";
    private static final String SUCCESS = "success";
    private Map<String, String> values = new HashMap<>();
    private boolean discard;
    private boolean invalid;

    public Flash() {
      //Empty constructor required for Google Guice
    }
    
    public static Flash create() {
        return new Flash();
    }
    
    public Flash withContent(Map<String, String> values) {
        Objects.requireNonNull(values, Required.VALUES);
        this.values = values;
        
        return this;
    }

    public void setError(String value) {
        if (validCharacters(value)) {
            values.put(ERROR, value);
        }
    }

    public void setWarning(String value) {
        if (validCharacters(value)) {
            values.put(WARNING, value);
        }
    }

    public void setSuccess(String value) {
        if (validCharacters(value)) {
            values.put(SUCCESS, value);
        }
    }

    public void put(String key, String value) {
        if (validCharacters(key) && validCharacters(value)) {
            values.put(key, value);
        }
    }
    
    /**
     * Expires the flash cookie on the client.
     */
    public void invalidate() {
        invalid = true;
    }

    public String get(String key) {
        return values.get(key);
    }

    public String remove(String key) {
        return values.remove(key);
    }

    public Map<String, String> getValues() {
        return values;
    }

    public boolean isDiscard() {
        return discard;
    }
    
    public Flash setDiscard(boolean discard) {
        this.discard = discard;

        return this;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean hasContent() {
        return !values.isEmpty();
    }

    private boolean validCharacters(String value) {
        if (INVALID_CHARACTERS.contains(value)) {
            LOG.error("Flash key or value can not contain the following characters: spaces, |, & or :");
            return false;
        }

        return true;
    }
}