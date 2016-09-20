package io.mangoo.utils.cookie;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.utils.DateUtils;

/**
 *
 * @author svenkubiak
 *
 */
public class CookieParser {
    private Map<String, String> sessionValues = new HashMap<>();
    private String secret;
    private String value;
    private String authenticityToken;
    private String authenticatedUser;
    private LocalDateTime expiresDate;
    private boolean encrypted;
    
    public CookieParser() {
    }
    
    public static CookieParser build() {
        return new CookieParser();
    }

    public CookieParser withContent(String value) {
        this.value = value;
        return this;
    }
    
    public CookieParser withSecret(String secret) {
        Objects.requireNonNull(secret, "application secret can not be null");
        
        this.secret = secret;
        return this;
    }
    
    public CookieParser isEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public boolean hasValidSessionCookie() {
        decrypt();

        boolean valid = false;
        if (StringUtils.isNotBlank(this.value)) {
            Jws<Claims> jwsClaims = Jwts.parser()
                .setSigningKey(this.secret)
                .parseClaimsJws(this.value);
            
            Claims claims = jwsClaims.getBody();
            Date expiration = claims.getExpiration();
            if (expiration != null && claims != null) {
                LocalDateTime expires = DateUtils.dateToLocalDateTime(expiration);
                if (LocalDateTime.now().isBefore(expires)) {
                    this.sessionValues = claims.get("data", Map.class);
                    this.authenticityToken = claims.get("authenticityToken", String.class); 
                    this.expiresDate = expires;  
                    valid = true;
                }
            }
        }
        
        return valid;
    }

    public boolean hasValidAuthenticationCookie() {
        decrypt();

        boolean valid = false;
        if (StringUtils.isNotBlank(this.value)) {
            Jws<Claims> jwsClaims = Jwts.parser()
                .setSigningKey(this.secret)
                .parseClaimsJws(this.value);
            
            Claims claims = jwsClaims.getBody();
            Date expiration = claims.getExpiration();
            if (expiration != null && claims != null) {
                LocalDateTime expires = DateUtils.dateToLocalDateTime(expiration);
                if (LocalDateTime.now().isBefore(expires)) {
                    this.authenticatedUser = claims.getSubject();
                    this.expiresDate = expires;
                    valid = true;                        
                }
            }
        }

        return valid;
    }

    public Map<String, String> getSessionValues() {
        return this.sessionValues;
    }

    public String getAuthenticityToken() {
        return this.authenticityToken;
    }

    public LocalDateTime getExpiresDate() {
        return this.expiresDate;
    }

    public String getAuthenticatedUser() {
        return this.authenticatedUser;
    }

    private void decrypt() {
        if (this.encrypted && !this.value.contains("\\|")) {
            this.value = Application.getInstance(Crypto.class).decrypt(this.value);
        }
    }
}