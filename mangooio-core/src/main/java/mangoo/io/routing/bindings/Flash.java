package mangoo.io.routing.bindings;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author svenkubiak
 *
 */
public class Flash {
    private Map<String, String> values = new HashMap<String, String>();
    private boolean discard;

    public Flash() {
    }

    public Flash(Map<String, String> values) {
        this.values = values;
    }

    public void setError(String value) {
        this.values.put("error", value);
    }

    public void setWarn(String value) {
        this.values.put("warn", value);
    }

    public void setSuccess(String value) {
        this.values.put("success", value);
    }

    public void add(String key, String value) {
        this.values.put(key, value);
    }

    public String get(String key) {
        return this.values.get(key);
    }

    public Map<String, String> getValues() {
        return this.values;
    }

    public boolean isDiscard() {
        return discard;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    public boolean hasContent() {
        return !this.values.isEmpty();
    }
}