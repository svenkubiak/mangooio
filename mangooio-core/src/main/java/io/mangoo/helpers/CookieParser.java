package io.mangoo.helpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.routing.handlers.DispatcherHandler;

/**
 *
 * @author svenkubiak
 *
 */
public class CookieParser {
    private static final Logger LOG = LogManager.getLogger(DispatcherHandler.class);
    private Map<String, String> sessionValues = new HashMap<>();
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
    
    public CookieParser isEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }
    
    public boolean isTwoFactor() {
        return this.twoFactor;
    }
    
    @SuppressWarnings("unchecked")
    public boolean hasValidSessionCookie() {
        if (this.encrypted && StringUtils.isNotBlank(this.value) && !this.value.contains("\\|")) {
            this.value = Application.getInstance(Crypto.class).decrypt(this.value, Application.getInstance(Config.class).getSessionCookieEncryptionKey());
        }

        boolean valid = false;
        if (StringUtils.isNotBlank(this.value)) {
            try {
                Jws<Claims> jwsClaims = Jwts.parser()
                        .setSigningKey(Application.getInstance(Config.class).getSessionCookieSignKey().getBytes(Charsets.UTF_8))
                        .parseClaimsJws(this.value);
                    
                Claims claims = jwsClaims.getBody();
                Date expiration = claims.getExpiration();
                if (expiration != null) {
                    this.sessionValues = claims.get(ClaimKey.DATA.toString(), Map.class);
                    this.authenticityToken = claims.get(ClaimKey.AUTHENTICITY.toString(), String.class); 
                    this.expiresDate = dateToLocalDateTime(expiration); 
                    valid = true;
                } 
            } catch (Exception e) { //NOSONAR
                LOG.error("Failed to parse JWS for seesion cookie", e);
            }
        }
        
        return valid;
    }

    public boolean hasValidAuthenticationCookie() {
        if (this.encrypted && StringUtils.isNotBlank(this.value) && !this.value.contains("\\|")) {
            this.value = Application.getInstance(Crypto.class).decrypt(this.value, Application.getInstance(Config.class).getAuthenticationCookieEncryptionKey());
        }

        boolean valid = false;
        if (StringUtils.isNotBlank(this.value)) {
            try {
                Jws<Claims> jwsClaims = Jwts.parser()
                        .setSigningKey(Application.getInstance(Config.class).getAuthenticationCookieSignKey().getBytes(Charsets.UTF_8))
                        .parseClaimsJws(this.value);
                    
                Claims claims = jwsClaims.getBody();
                Date expiration = claims.getExpiration();
                if (expiration != null) {
                    this.authenticatedUser = claims.getSubject();
                    this.twoFactor = claims.get(ClaimKey.TWO_FACTOR.toString(), Boolean.class);
                    this.expiresDate = dateToLocalDateTime(expiration);
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
    
    private LocalDateTime dateToLocalDateTime(Date date) {
        Objects.requireNonNull(date, Required.DATE.toString());
        
        Instant instant = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}