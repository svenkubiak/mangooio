package io.mangoo.utils.paseto;

import io.mangoo.constants.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Token {
    private String id;
    private String issuer;
    private String subject;
    private Map<String, String> claims = new HashMap<>();
    private LocalDateTime expires;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getExpires() {
        return expires;
    }

    public void setExpires(LocalDateTime expires) {
        this.expires = expires;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Map<String, String> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, String> claims) {
        this.claims = claims;
    }

    public String getClaim(String key) {
        Objects.requireNonNull(key, NotNull.KEY);
        return claims.get(key);
    }

    public boolean containsClaim(String key) {
        Objects.requireNonNull(key, NotNull.KEY);
        return claims.containsKey(key);
    }

    public boolean getClaimAsBoolean(String key) {
        Objects.requireNonNull(key, NotNull.KEY);
        String value = claims.get(key);

        return StringUtils.isNotBlank(value) && Boolean.parseBoolean(value);
    }
}
