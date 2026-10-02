package io.mangoo.routing.bindings;

import com.google.common.base.Preconditions;
import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Const;
import io.mangoo.constants.Required;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Session {
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
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(value, Required.VALUE);
        // Session values are stored as claims of a JWT, a reserved or internal claim name would break the whole cookie
        Preconditions.checkArgument(!ClaimKey.RESERVED.contains(key) && !Const.CSRF_TOKEN.equals(key), "Session key '" + key + "' is reserved");
        values.put(key, value);
        changed = true;
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