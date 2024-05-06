package io.mangoo.routing.bindings;

import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.totp.TotpUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Authentication {
    private LocalDateTime expires;
    private String subject;
    private boolean twoFactor;
    private boolean remember;
    private boolean loggedOut;
    private boolean invalid;
    
    public static Authentication create() {
        return new Authentication();
    }
    
    public Authentication withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, NotNull.EXPIRES);
        if (this.expires == null) {
            this.expires = expires;            
        }
        
        return this;
    }
    
    public Authentication withSubject(String subject) {
        if (StringUtils.isBlank(this.subject)) {
            this.subject = subject;            
        }
        
        return this;
    }

    /**
     * Retrieves the current subject
     *
     * @return The subject of the current authentication or null if not set
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * Invalidates the authentication by sending expiring the client cookie
     */
    public void invalidate() {
        invalid = true;
    }

    /**
     * Returns the LocalDateTime when the authentication expires
     *
     * @return A LocalDateTime object or null if not set
     */
    public LocalDateTime getExpires() {
        return expires;
    }

    /**
     * @return True if the user wants to log out, false otherwise
     */
    public boolean isLogout() {
        return loggedOut;
    }

    /**
     *
     * @return True if the user wants to stay logged in, false otherwise
     */
    public boolean isRememberMe() {
        return remember;
    }
    
    /**
     * @return True if two-factor authentication is enabled for this user
     */
    public boolean isTwoFactor() {
        return twoFactor;
    }
    
    /**
     * Creates a hashed value of a given clear text password and checks if the
     * value matches a given, already hashed password
     *
     * @param identifier The identifier to authenticate
     * @param password The clear text password
     * @param salt The salt to use for hashing
     * @param hash The previously hashed password to check
     * @return True if the new hashed password matches the hash, false otherwise
     */
    public boolean validLogin(String identifier, String password, String salt, String hash) {
        Objects.requireNonNull(identifier, NotNull.USERNAME);
        Objects.requireNonNull(password, NotNull.PASSWORD);
        Objects.requireNonNull(password, NotNull.SALT);
        Objects.requireNonNull(hash, NotNull.HASH);

        var cache = Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);
        var authenticated = false;
        if (!userHasLock(identifier) && CodecUtils.matchArgon2(password, salt, hash)) {
            authenticated = true;
        } else {
            cache.getAndIncrementCounter(identifier);
        }

        return authenticated;
    }
    
    /**
     * Performs a login by setting the authentication to the given identifier
     * Please note, that calling validLogin is mandatory before this call!
     * 
     * @param subject The subject to login
     * @return Authentication object
     */
    public Authentication login(String subject) {
        this.subject = subject;
        return this;
    }
    
    /**
     * Sets remember me functionality, default is false
     * 
     * @param remember The state of remember to set
     * @return Authentication object
     */
    public Authentication rememberMe(boolean remember) {
        this.remember = remember;
        return this;
    }
    
    /**
     * Sets remember me functionality to true, default is false
     * @return Authentication object
     */
    public Authentication rememberMe() {
        this.remember = true;
        return this;
    }
    
    /**
     * Sets the requirement of the two-factor authentication, default is false
     * 
     * @param twoFactor True for enabling two-factor authentication, false otherwise
     * @return Authentication object
     */
    public Authentication twoFactorAuthentication(boolean twoFactor) {
        this.twoFactor = twoFactor;
        return this;
    }
 
    /**
     * Checks if a username is locked because of to many failed login attempts
     * 
     * @param username The username to check
     * @return true if the user has a lock, false otherwise
     */
    public boolean userHasLock(String username) {
        Objects.requireNonNull(username, NotNull.USERNAME);
        var lock = false;
        
        var config = Application.getInstance(Config.class);
        var cache = Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);
        AtomicInteger counter = cache.getCounter(username);
        if (counter != null && counter.get() > config.getAuthenticationLock()) {
            lock = true;
        }
        
        return lock;
    }
    
    /**
     * Checks if a given number for 2FA is valid for the given secret
     * 
     * @param secret The plaintext secret to use for checking
     * @param number The number entered by the user
     * @return True if number is valid, false otherwise
     */
    public boolean validSecondFactor(String secret, String number) {
        Objects.requireNonNull(secret, NotNull.SECRET);
        Objects.requireNonNull(number, NotNull.TOTP);
        
        return TotpUtils.verifiedTotp(secret, number);
    }

    /**
     * Performs a logout of the currently authenticated user
     */
    public void logout() {
        loggedOut = true;
    }
    
    /**
     * Checks if the authentication class contains an authentication
     *
     * @return True if authentication contains an authentication, false otherwise
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(subject);
    }
    
    public boolean isInvalid() {
        return invalid;
    }
}