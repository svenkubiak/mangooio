package io.mangoo.routing.bindings;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mangoo.core.Bootstrap;


/**
 *
 * @author svenkubiak
 *
 */
public class Session {
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);
    private Map<String, String> values = new HashMap<String, String>();
    private String authenticityToken;
    private boolean changed;
    private LocalDateTime expires;

    public Session() {
    }

    public Session(Map<String, String> values) {
        this.values = values;
    }

    public boolean hasContent() {
        return !this.values.isEmpty();
    }

    public String get(String key) {
        return this.values.get(key);
    }

    /**
     * Adds a value to the cache.
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
     * Removes a value with a given key from the cache
     *
     * @param key The key to remove
     */
    public void remove(String key) {
        this.changed = true;
        this.values.remove(key);
    }

    /**
     * Clears the complete cache
     */
    public void clear() {
        this.changed = true;
        this.values = new HashMap<String, String>();
    }

    public boolean hasChanges() {
        return this.changed;
    }

    public Map<String, String> getValues() {
        return this.values;
    }

    public LocalDateTime getExpires() {
        return expires;
    }

    public void setExpires(LocalDateTime localDateTime) {
        this.expires = localDateTime;
    }

    public String getAuthenticityToken() {
        this.changed = true;
        return authenticityToken;
    }

    public void setAuthenticityToken(String authenticityToken) {
        this.authenticityToken = authenticityToken;
    }
}