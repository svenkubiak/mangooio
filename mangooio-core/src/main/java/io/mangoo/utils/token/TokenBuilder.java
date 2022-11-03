package io.mangoo.utils.token;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import dev.paseto.jpaseto.PasetoV2LocalBuilder;
import dev.paseto.jpaseto.Pasetos;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.utils.MangooUtils;

public class TokenBuilder extends TokenCommons {
    private LocalDateTime expires;
    private String sharedSecret;
    private Map<String, Object> claims = new HashMap<>();
    private String subject;
    
    public static TokenBuilder create() {
        return new TokenBuilder();
    }

    public TokenBuilder withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, Required.EXPIRES.toString());
        this.expires = expires;

        return this;
    }
    
    public TokenBuilder withSharedSecret(String sharedSecret) {
        Objects.requireNonNull(sharedSecret, Required.SHARED_SECRET.toString());
        this.sharedSecret = sharedSecret;

        return this;
    }
    
    public TokenBuilder withClaim(ClaimKey claimKey, Object value) {
        withClaim(claimKey.toString(), value);
        return this;
    }
    
    public TokenBuilder withClaim(String key, Object value) {
        Objects.requireNonNull(key, Required.KEY.toString());
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
                    .setExpiration(expires.toInstant(ZONE_OFFSET))
                    .setSubject(subject == null ? MangooUtils.randomString(32) : subject)
                    .setSharedSecret(new SecretKeySpec(sharedSecret.getBytes(CHARSET), ALGORITHM));
             
             claims.entrySet()
                 .stream()
                 .forEach(entry -> pasetoBuilder.claim(entry.getKey(), entry.getValue()));
             
             return pasetoBuilder.compact();
        } catch (Exception e) {
            throw new MangooTokenException(e);
        }
    }
}