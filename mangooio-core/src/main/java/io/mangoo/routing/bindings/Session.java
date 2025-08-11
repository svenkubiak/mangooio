package io.mangoo.routing.bindings;

import io.mangoo.constants.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Session {
    private static final Logger LOG = LogManager.getLogger(Session.class);
    private static final Set<String> INVALID_CHARACTERS = Set.of("|", ":", "&", " ");
    private Map<String, String> values = new HashMap<>();
    private String csrf;
    private LocalDateTime expires;
    private boolean changed;
    private boolean invalid;

    public static Session create() {
        return new Session();
    }
    
    public Session withContent(Map<String, String> values) {
        Objects.requireNonNull(values, NotNull.VALUES);
        
        this.values = values;
        return this;
    }
    
    public Session withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, NotNull.EXPIRES);
        
        this.expires = expires;
        return this;
    }

    public Session withCsrf(String csrf) {
        Objects.requireNonNull(csrf, NotNull.CSRF);

        this.csrf = csrf;
        return this;
    }
    
    /**
     * Invalidates the session by sending expiring the client cookie
     */
    public void invalidate() {
        invalid = true;
    }

    /**
     * Checks if the session has at least one entry
     *
     * @return True if the session has at least one entry, false otherwise
     */
    public boolean hasContent() {
        return !values.isEmpty();
    }

    /**
     * Retrieves a specific value from the session
     *
     * @param key The key
     * @return The value or null if none present
     */
    public String get(String key) {
        return values.get(key);
    }

    /**
     * @return All values of the session
     */
    public Map<String, String> getValues() {
        return values;
    }

    /**
     * @return The expiry date of the session
     */
    public LocalDateTime getExpires() {
        return expires;
    }

    /**
     * @return The csrf token of the session
     */
    public String getCsrf() {
        return csrf;
    }

    /**
     * Adds a value to the session, overwriting an existing value
     *
     * @param key The key to store the value
     * @param value The value to store
     */
    public void put(String key, String value) {
        if (INVALID_CHARACTERS.contains(key) || INVALID_CHARACTERS.contains(value)) {
            LOG.error("Session key or value can not contain the following characters: spaces, |, & or :");
        }  else {
            changed = true;
            values.put(key, value);
        }
    }

    /**
     * Removes a value with a given key from the session
     *
     * @param key The key to remove
     */
    public void remove(String key) {
        changed = true;
        values.remove(key);
    }

    /**
     * Clears the complete session
     */
    public void clear() {
        values = new HashMap<>();
        invalid = true;
    }

    /**
     * @return True if a session values has changed, be removed or the session has been cleared, false otherwise
     */
    public boolean hasChanges() {
        return changed;
    }
    
    public boolean isInvalid() {
        return invalid;
    }
}