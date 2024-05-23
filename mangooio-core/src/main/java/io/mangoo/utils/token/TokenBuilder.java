package io.mangoo.utils.token;

import dev.paseto.jpaseto.PasetoV2LocalBuilder;
import dev.paseto.jpaseto.Pasetos;
import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.utils.MangooUtils;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TokenBuilder {
    private static final String ALGORITHM = "AES";
    private final Map<String, Object> claims = new HashMap<>();
    private LocalDateTime expires;
    private String sharedSecret;
    private String subject;
    
    public static TokenBuilder create() {
        return new TokenBuilder();
    }

    public TokenBuilder withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, NotNull.EXPIRES);
        this.expires = expires;

        return this;
    }
    
    public TokenBuilder withSharedSecret(String sharedSecret) {
        Objects.requireNonNull(sharedSecret, NotNull.SHARED_SECRET);
        this.sharedSecret = sharedSecret;

        return this;
    }

    public TokenBuilder withClaim(String key, Object value) {
        Objects.requireNonNull(key, NotNull.KEY);
        claims.put(key, value);

        return this;
    }
    
    public TokenBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }
    
    public LocalDateTime getExpires() {
        return expires;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public String getSubject() {
        return subject;
    }

    public String build() throws MangooTokenException {
        try {
             PasetoV2LocalBuilder pasetoBuilder = Pasetos.V2.LOCAL.builder()
                    .setExpiration(expires.toInstant(ZoneOffset.UTC))
                    .setSubject(subject == null ? MangooUtils.randomString(32) : subject)
                    .setSharedSecret(new SecretKeySpec(sharedSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM));

            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                pasetoBuilder.claim(entry.getKey(), entry.getValue());
            }

            return pasetoBuilder.compact();
        } catch (Exception e) {
            throw new MangooTokenException(e);
        }
    }
}