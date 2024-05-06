package io.mangoo.utils.token;

import dev.paseto.jpaseto.Paseto;
import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class Token {
    private final Paseto paseto;
    
    public Token(Paseto paseto) {
        this.paseto = Objects.requireNonNull(paseto, NotNull.PASETO);
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
        Objects.requireNonNull(claimKey, NotNull.CLAIM_KEY);
        
        return containsClaim(claimKey);
    }
    
    /**
     * @param key The claim key to check
     * 
     * @return True if the claim exists, false otherwise
     */
    public boolean containsClaim(String key) {
        Objects.requireNonNull(key, NotNull.KEY);
        
        return paseto.getClaims().containsKey(key);
    }

    /**
     * 
     * @param localDateTime The localDateTime to check against
     * 
     * @return True if the expiration is after the given localDateTime, false otherwise
     */
    public boolean expirationIsAfter(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, NotNull.LOCAL_DATE_TIME);
        
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
        Objects.requireNonNull(claimKey, NotNull.CLAIM_KEY);
        Objects.requireNonNull(clazz, NotNull.CLASS);
        
        return getClaim(claimKey, clazz);
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
        Objects.requireNonNull(key, NotNull.KEY);
        Objects.requireNonNull(clazz, NotNull.CLASS);
        
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