package io.mangoo.routing.bindings;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author svenkubiak
 *
 */
public class Session {
    private static final Logger LOG = LogManager.getLogger(Session.class);
    private Map<String, String> values;
    private String authenticityToken;
    private boolean changed;
    private LocalDateTime expires;
    
    public Session(){
    }

    public Session(Map<String, String> values, String authenticityToken, LocalDateTime expires) {
        this.values = Optional.ofNullable(values).orElse(new HashMap<String, String>());
        this.authenticityToken = authenticityToken;
        this.expires = expires;
    }

    public boolean hasContent() {
        return !this.values.isEmpty();
    }

    public String get(String key) {
        return this.values.get(key);
    }

    public Map<String, String> getValues() {
        return this.values;
    }

    public LocalDateTime getExpires() {
        return this.expires;
    }

    /**
     * Adds a value to the session.
     *
     * @param key The key to store the value
     * @param value The value to store
     */
    public void add(String key, String value) {
        if (key.contains("|") || key.contains(":") || key.contains("&")) {
            LOG.error("Invalid characters found in session key. Please note, that the key can not contain |, : or &");
        } else if (value.contains("|") || value.contains(":") || value.contains("&")) {
            LOG.error("Invalid characters found in session value. Please note, that the value can not contain |, : or &");
        } else {
            this.changed = true;
            this.values.put(key, value);
        }
    }

    /**
     * Removes a value with a given key from the session
     *
     * @param key The key to remove
     */
    public void remove(String key) {
        this.changed = true;
        this.values.remove(key);
    }

    /**
     * Clears the complete session
     */
    public void clear() {
        this.changed = true;
        this.values = new HashMap<String, String>();
    }

    /**
     * @return True if a session values has change, be removed or the session has been cleared, false otherwise
     */
    public boolean hasChanges() {
        return this.changed;
    }

    /**
     * @return The current authenticity token and marks the session as changed
     */
    public String getAuthenticityToken() {
        this.changed = true;
        return this.authenticityToken;
    }
}