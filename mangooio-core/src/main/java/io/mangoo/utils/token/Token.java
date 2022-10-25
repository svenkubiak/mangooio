package io.mangoo.utils.token;

import java.time.LocalDateTime;
import java.util.Objects;

import dev.paseto.jpaseto.Paseto;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
public class Token extends TokenCommons {
    private Paseto paseto;
    
    public Token(Paseto paseto) {
        this.paseto = Objects.requireNonNull(paseto, "paseto can not be null");
    }

    public LocalDateTime getExpiration() {
        return LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZONE_OFFSET);
    }

    public boolean containsClaim(ClaimKey claimKey) {
        Objects.requireNonNull(claimKey, Required.CLAIM_KEY.toString());
        
        return containsClaim(claimKey.toString());
    }
    
    public boolean containsClaim(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        
        return paseto.getClaims().containsKey(key);
    }

    public boolean expirationIsAfter(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, Required.LOCAL_DATE_TIME.toString());
        
        return getExpiration().isAfter(localDateTime);
    }
    
    public <T> T getClaim(ClaimKey claimKey, Class<T> clazz) {
        Objects.requireNonNull(claimKey, Required.CLAIM_KEY.toString());
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        
        return getClaim(ClaimKey.FORM.toString(), clazz);
    }
    
    public <T> T getClaim(String key, Class<T> clazz) {
        Objects.requireNonNull(key, Required.KEY.toString());
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        
        return paseto.getClaims().get(key, clazz);
    }

    public String getSubject() {
        return paseto.getClaims().getSubject();
    }
    
    public Paseto getPaseto() {
        return paseto;
    }
}