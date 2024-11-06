package io.mangoo.utils.jwt;

import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.JsonUtils;
import io.mangoo.utils.MangooUtils;
import org.apache.logging.log4j.util.Strings;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.PasetoLocal;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JwtBuilder {
    private Map<String, String> claims = new HashMap<>();
    private LocalDateTime expires;
    private String sharedSecret;
    private String subject;
    
    public static JwtBuilder create() {
        return new JwtBuilder();
    }

    public JwtBuilder withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, NotNull.EXPIRES);
        this.expires = expires;

        return this;
    }
    
    public JwtBuilder withSharedSecret(String sharedSecret) {
        Objects.requireNonNull(sharedSecret, NotNull.SHARED_SECRET);
        this.sharedSecret = sharedSecret;

        return this;
    }

    public JwtBuilder withClaim(String key, String value) {
        Objects.requireNonNull(key, NotNull.KEY);
        claims.put(key, value);

        return this;
    }

    public JwtBuilder withClaims(Map<String, String> claims) {
        this.claims = claims;

        return this;
    }
    
    public JwtBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String build() throws MangooTokenException {
        try {
            Jwt jwt = new Jwt();
            jwt.setExpires(expires);
            jwt.setSubject(subject == null ? MangooUtils.randomString(32) : subject);
            jwt.setId(CodecUtils.uuid());
            jwt.setClaims(claims);

            return PasetoLocal.encrypt(
                    new SecretKey(sharedSecret.substring(0, 32).getBytes(StandardCharsets.UTF_8), Version.V4),
                    JsonUtils.toJson(jwt),
                    Strings.EMPTY,
                    Strings.EMPTY);
        } catch (Exception e) {
            throw new MangooTokenException(e);
        }
    }

    public Map<String, String> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, String> claims) {
        this.claims = claims;
    }

    public LocalDateTime getExpires() {
        return expires;
    }

    public void setExpires(LocalDateTime expires) {
        this.expires = expires;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}