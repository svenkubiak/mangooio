package io.mangoo.utils.jwt;

import io.mangoo.constants.NotNull;

import java.time.LocalDateTime;
import java.util.*;

public class Jwt {
    private String id;
    private String issuer;
    private String subject;
    private List<String> audience = new ArrayList<>();
    private Map<String, String> claims = new HashMap<>();
    private LocalDateTime expires;
    private LocalDateTime notBefore;
    private LocalDateTime issuedAt;

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

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(LocalDateTime notBefore) {
        this.notBefore = notBefore;
    }

    public List<String> getAudience() {
        return audience;
    }

    public void setAudience(List<String> audience) {
        this.audience = audience;
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
}
