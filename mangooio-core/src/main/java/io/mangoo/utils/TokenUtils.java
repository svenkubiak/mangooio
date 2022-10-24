package io.mangoo.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.PasetoException;
import dev.paseto.jpaseto.PasetoV2LocalBuilder;
import dev.paseto.jpaseto.Pasetos;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;

public final class TokenUtils {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;
    private static final String ALGORITHM = "AES";

    /**
     * Creates a paseto token
     * 
     * @param expires The expiration date of the token
     * @param sharedSecret The shared secret to be uses for the token
     * @param claims A Map of claims 
     * @param subject The subject to be used for the token
     * 
     * @return A string representation of the paseto token
     */
    public static String getToken(LocalDateTime expires, String sharedSecret, Map<ClaimKey, Object> claims, String subject) {
        Objects.requireNonNull(expires, Required.EXPIRES.toString());
        Objects.requireNonNull(sharedSecret, Required.SHARED_SECRET.toString());
        Objects.requireNonNull(claims, Required.CLAIMS.toString());
        
         PasetoV2LocalBuilder pasetoBuilder = Pasetos.V2.LOCAL.builder()
                .setExpiration(expires.toInstant(ZONE_OFFSET))
                .setSubject(subject == null ? MangooUtils.randomString(32) : subject)
                .setSharedSecret(new SecretKeySpec(sharedSecret.getBytes(CHARSET), ALGORITHM));
         
         for (Entry<ClaimKey, Object> entry : claims.entrySet()) {
             pasetoBuilder.claim(entry.getKey().toString(), entry.getValue());
         }
         
         return pasetoBuilder.compact();
    }
    
    /**
     * Parses a given cookie value to a paseto token
     * 
     * @param sharedSecret The shared secret used for the token creation
     * @param cookieValue The cookie value to parse the token from
     * @return A valid paseto token
     * @throws PasetoException When parsing of the cookie value fails
     */
    public static Paseto parseToken(String sharedSecret, String cookieValue) throws PasetoException {
        Objects.requireNonNull(sharedSecret, Required.SHARED_SECRET.toString());
        Objects.requireNonNull(cookieValue, Required.COOKIE_VALUE.toString());
        
        try {
            Paseto paseto = Pasetos.parserBuilder()
                .setSharedSecret(sharedSecret.getBytes(CHARSET))
                .build()
                .parse(cookieValue);
            
            return paseto;
        } catch (PasetoException e) {
           throw new PasetoException("Failed to parse cookie value"); 
        }
    }
}