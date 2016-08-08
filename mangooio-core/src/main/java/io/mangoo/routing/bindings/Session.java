package io.mangoo.routing.bindings;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

/**
 *
 * @author svenkubiak
 *
 */
public class Session {
    private static final Logger LOG = LogManager.getLogger(Session.class);
    private static final Set<String> BLACKLIST = Sets.newHashSet("|", ":", "&", " ");
    private Map<String, String> values;
    private String authenticityToken;
    private boolean changed;
    private LocalDateTime expires;

    public Session(){
    }

    public Session(Map<String, String> values, String authenticityToken, LocalDateTime expires) {
        this.values = Optional.ofNullable(values).orElse(new HashMap<>());
        this.authenticityToken = authenticityToken;
        this.expires = expires;
    }

    /**
     * Checks if the session has at least one entry
     *
     * @return True if the session has at least one entry, false otherwise
     */
    public boolean hasContent() {
        return !this.values.isEmpty();
    }

    /**
     * Retrieves a specific value from the session
     *
     * @param key The key
     * @return The value or null if none present
     */
    public String get(String key) {
        return this.values.get(key);
    }

    /**
     * @return All values of the session
     */
    public Map<String, String> getValues() {
        return this.values;
    }

    /**
     * @return The expire date of the session
     */
    public LocalDateTime getExpires() {
        return this.expires;
    }

    /**
     * Adds a value to the session, overwriting an existing value
     *
     * @param key The key to store the value
     * @param value The value to store
     */
    public void put(String key, String value) {
        if (BLACKLIST.contains(key) || BLACKLIST.contains(value)) {
            LOG.error("Session key or value can not contain the following characters: spaces, |, & or :");
        }  else {
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
        this.values = new HashMap<>();
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