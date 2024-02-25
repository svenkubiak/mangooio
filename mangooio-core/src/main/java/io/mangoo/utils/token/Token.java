package io.mangoo.utils.token;

import dev.paseto.jpaseto.Paseto;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class Token {
    private final Paseto paseto;
    
    public Token(Paseto paseto) {
        this.paseto = Objects.requireNonNull(paseto, Required.PASETO.toString());
    }

    /**
     * @return The LocalDateTime representation of the expiration with ZoneOffset.UTC
     */
    public LocalDateTime getExpiration() {
        return LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZoneOffset.UTC);
    }

    /**
     * @param claimKey The claim key to check
     * 
     * @return True if the claim exists, false otherwise
     */
    public boolean containsClaim(ClaimKey claimKey) {
        Objects.requireNonNull(claimKey, Required.CLAIM_KEY.toString());
        
        return containsClaim(claimKey.toString());
    }
    
    /**
     * @param key The claim key to check
     * 
     * @return True if the claim exists, false otherwise
     */
    public boolean containsClaim(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        
        return paseto.getClaims().containsKey(key);
    }

    /**
     * 
     * @param localDateTime The localDateTime to check against
     * 
     * @return True if the expiration is after the given localDateTime, false otherwise
     */
    public boolean expirationIsAfter(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, Required.LOCAL_DATE_TIME.toString());
        
        return getExpiration().isAfter(localDateTime);
    }
    
    /**
     * 
     * @param <T> undefined
     * @param claimKey The claim key to check
     * @param clazz The class to convert to
     * 
     * @return The claim value converted to the given class to convert
     */
    public <T> T getClaim(ClaimKey claimKey, Class<T> clazz) {
        Objects.requireNonNull(claimKey, Required.CLAIM_KEY.toString());
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        
        return getClaim(claimKey.toString(), clazz);
    }
    
    /**
     * 
     * @param <T> undefined
     * @param key The key to check
     * @param clazz The class to convert to
     * 
     * @return The claim value converted to the given class to convert
     */
    public <T> T getClaim(String key, Class<T> clazz) {
        Objects.requireNonNull(key, Required.KEY.toString());
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        
        return paseto.getClaims().get(key, clazz);
    }

    /**
     * @return The subject of the claim
     */
    public String getSubject() {
        return paseto.getClaims().getSubject();
    }
    
    /**
     * @return The raw paseto object
     */
    public Paseto getPaseto() {
        return paseto;
    }
}