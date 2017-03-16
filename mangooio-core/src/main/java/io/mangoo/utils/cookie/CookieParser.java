package io.mangoo.utils.cookie;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.routing.handlers.DispatcherHandler;
import io.mangoo.utils.DateUtils;

/**
 *
 * @author svenkubiak
 *
 */
public class CookieParser {
    private static final Logger LOG = LogManager.getLogger(DispatcherHandler.class);
    private Map<String, String> sessionValues = new HashMap<>();
    private String secret;
    private String value;
    private String authenticityToken;
    private String authenticatedUser;
    private LocalDateTime expiresDate;
    private boolean encrypted;
    private boolean twoFactor;
    
    public static CookieParser build() {
        return new CookieParser();
    }

    public CookieParser withContent(String value) {
        this.value = value;
        return this;
    }
    
    public CookieParser withSecret(String secret) {
        Objects.requireNonNull(secret, Required.APPLICATION_SECRET.toString());
        
        this.secret = secret;
        return this;
    }
    
    public CookieParser isEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }
    
    public boolean isTwoFactor() {
        return this.twoFactor;
    }
    
    @SuppressWarnings("unchecked")
    public boolean hasValidSessionCookie() {
        decrypt();

        boolean valid = false;
        if (StringUtils.isNotBlank(this.value)) {
            try {
                Jws<Claims> jwsClaims = Jwts.parser()
                        .setSigningKey(this.secret)
                        .parseClaimsJws(this.value);
                    
                Claims claims = jwsClaims.getBody();
                Date expiration = claims.getExpiration();
                if (expiration != null) {
                    this.sessionValues = claims.get(ClaimKey.DATA.toString(), Map.class);
                    this.authenticityToken = claims.get(ClaimKey.AUTHENTICITY.toString(), String.class); 
                    this.expiresDate = DateUtils.dateToLocalDateTime(expiration);  
                    valid = true;
                } 
            } catch (Exception e) { //NOSONAR
                LOG.error("Failed to parse JWS for seesion cookie", e);
            }
        }
        
        return valid;
    }

    public boolean hasValidAuthenticationCookie() {
        decrypt();

        boolean valid = false;
        if (StringUtils.isNotBlank(this.value)) {
            try {
                Jws<Claims> jwsClaims = Jwts.parser()
                        .setSigningKey(this.secret)
                        .parseClaimsJws(this.value);
                    
                Claims claims = jwsClaims.getBody();
                Date expiration = claims.getExpiration();
                if (expiration != null) {
                    this.authenticatedUser = claims.getSubject();
                    this.twoFactor = claims.get(ClaimKey.TWO_FACTOR.toString(), Boolean.class);
                    this.expiresDate = DateUtils.dateToLocalDateTime(expiration);
                    valid = true;                        
                }  
            } catch (Exception e) { //NOSONAR
                LOG.error("Failed to parse JWS for authentication cookie", e);
            }
        }

        return valid;
    }

    public Map<String, String> getSessionValues() {
        return this.sessionValues;
    }

    public String getAuthenticity() {
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