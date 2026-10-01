package io.mangoo.routing.bindings;

import io.mangoo.constants.Required;
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
    private boolean keep;

    public static Session create() {
        return new Session();
    }
    
    public Session withContent(Map<String, String> values) {
        Objects.requireNonNull(values, Required.VALUES);
        
        this.values = values;
        return this;
    }
    
    public Session withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, Required.EXPIRES);
        
        this.expires = expires;
        return this;
    }

    public Session withCsrf(String csrf) {
        Objects.requireNonNull(csrf, Required.CSRF);

        this.csrf = csrf;
        return this;
    }

    /**
     * Expires the session cookie on the client.
     */
    public void invalidate() {
        invalid = true;
    }

    public void keep() {
        keep = true;
    }

    public boolean hasContent() {
        return !values.isEmpty();
    }

    public String get(String key) {
        return values.get(key);
    }

    public Map<String, String> getValues() {
        return values;
    }

    public LocalDateTime getExpires() {
        return expires;
    }

    public String getCsrf() {
        return csrf;
    }

    public void put(String key, String value) {
        if (INVALID_CHARACTERS.contains(key) || INVALID_CHARACTERS.contains(value)) {
            LOG.error("Session key or value can not contain the following characters: spaces, |, & or :");
        }  else {
            values.put(key, value);
            changed = true;
        }
    }

    public void remove(String key) {
        values.remove(key);
        changed = true;
    }

    public void clear() {
        values = new HashMap<>();
        invalid = true;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean hasChanged() {
        return changed;
    }

    public boolean isKept() {
        return keep;
    }
}