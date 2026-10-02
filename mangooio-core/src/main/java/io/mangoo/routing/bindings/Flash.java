package io.mangoo.routing.bindings;

import com.google.common.base.Preconditions;
import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Required;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Flash {
    private static final String ERROR = "error";
    private static final String WARNING = "warning";
    private static final String SUCCESS = "success";
    private Map<String, String> values = new HashMap<>();
    private boolean discard;
    private boolean invalid;

    public Flash() {
      // Empty constructor for Google Guice
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
        Objects.requireNonNull(value, Required.VALUE);
        values.put(ERROR, value);
    }

    public void setWarning(String value) {
        Objects.requireNonNull(value, Required.VALUE);
        values.put(WARNING, value);
    }

    public void setSuccess(String value) {
        Objects.requireNonNull(value, Required.VALUE);
        values.put(SUCCESS, value);
    }

    public void put(String key, String value) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(value, Required.VALUE);
        // Flash values are stored as claims of a JWT, a reserved or internal claim name would break the whole cookie
        Preconditions.checkArgument(!ClaimKey.RESERVED.contains(key) && !ClaimKey.FORM.equals(key), "Flash key '" + key + "' is reserved");
        values.put(key, value);
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
}