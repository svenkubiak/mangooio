package io.mangoo.routing.bindings;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.HmacShaAlgorithm;
import io.mangoo.enums.Required;
import io.mangoo.models.OAuthUser;
import io.mangoo.providers.CacheProvider;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.CryptoUtils;
import io.mangoo.utils.TotpUtils;

/**
 * Convenient class for handling authentication
 *
 * @author svenkubiak
 *
 */
public class Authentication {
    private Config config;
    private LocalDateTime expires;
    private OAuthUser oAuthUser;
    private String identifier;
    private Cache cache;
    private boolean twoFactor;
    private boolean remember;
    private boolean loggedOut;

    @Inject
    public Authentication(CacheProvider cacheProvider, Config config) {
        Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
        Objects.requireNonNull(config, Required.CONFIG.toString());
        
        this.cache = cacheProvider.getCache(CacheName.AUTH);
        this.config = config;
    }
    
    public Authentication withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, Required.EXPIRES.toString());
        if (this.expires == null) {
            this.expires = expires;            
        }
        
        return this;
    }
    
    public Authentication withIdentifier(String identifier) {
        if (StringUtils.isBlank(this.identifier)) {
            this.identifier = identifier;            
        }
        
        return this;
    }

    /**
     * Retrieves the current identifier
     *
     * @return The identifier of the current authenticated user or null
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the LocalDateTime when the authentication expires
     *
     * @return A LocalDateTime object or null if unset
     */
    public LocalDateTime getExpires() {
        return this.expires;
    }

    /**
     * @return True if the user wants to logout, false otherwise
     */
    public boolean isLogout() {
        return this.loggedOut;
    }

    /**
     *
     * @return True if the user wants to stay logged in, false otherwise
     */
    public boolean isRememberMe() {
        return this.remember;
    }
    
    /**
     * @return True if two factor authentication is enabled for this user
     */
    public boolean isTwoFactor() {
        return this.twoFactor;
    }

    /**
     * Sets an OAuthUser to the current authentication.
     * Can only be set once!
     *
     * @param oAuthUser An OAuthUser
     */
    public void withOAuthUser(OAuthUser oAuthUser) {
        if (this.oAuthUser == null) {
            this.oAuthUser = oAuthUser;
        }
    }

    /**
     * Retrieves the current OAuthUser from the authentication
     * Note: This is only available during a OAuth authentication in a
     * method that is annotated with @OAuthCallbackFilter
     *
     * @return The current OAuthUser instance or null if undefined
     */
    public OAuthUser getOAuthUser() {
        return this.oAuthUser;
    }

    /**
     * Creates a hashed value of a given clear text password and checks if the
     * value matches a given, already hashed password
     * 
     * @deprecated  As of release 4.3.0, replaced by {@link #validLogin(String, String, String)}, will be removed in 5.0.0
     *
     * @param username The username to authenticate
     * @param password The clear text password
     * @param hash The previously hashed password to check
     * @return True if the new hashed password matches the hash, false otherwise
     */
    @Deprecated
    public boolean login(String username, String password, String hash) {
        Objects.requireNonNull(username, Required.USERNAME.toString());
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        Objects.requireNonNull(hash, Required.HASH.toString());

        boolean authenticated = false;
        if (!userHasLock(username) && CodecUtils.checkJBCrypt(password, hash)) {
            this.identifier = username;
            authenticated = true;
        } else {
            this.cache.increment(username);
        }

        return authenticated;
    }
    
    /**
     * Creates a hashed value of a given clear text password and checks if the
     * value matches a given, already hashed password
     *
     * @param subject The subject to authenticate
     * @param password The clear text password
     * @param hash The previously hashed password to check
     * @return True if the new hashed password matches the hash, false otherwise
     */
    public boolean validLogin(String subject, String password, String hash) {
        Objects.requireNonNull(subject, Required.USERNAME.toString());
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        Objects.requireNonNull(hash, Required.HASH.toString());

        boolean authenticated = false;
        if (!userHasLock(subject) && CodecUtils.checkJBCrypt(password, hash)) {
            authenticated = true;
        } else {
            this.cache.increment(subject);
        }

        return authenticated;
    }
    
    /**
     * Performs a login by setting the authenticated user to the given subject
     * Please note, that calling validLogin is mandatory before this call!
     * 
     * @deprecated As of version 4.13.0, use login() instead
     * 
     * @param subject The subject to login
     * @return Authentication object
     */
    @Deprecated
    public Authentication login(String subject) {
        this.identifier = subject;
        return this;
    }
    
    /**
     * Performs a login by setting the authenticated user to the given subject
     * Please note, that calling validLogin is mandatory before this call!
     * 
     * @return Authentication object
     */
    public Authentication login() {
        this.identifier = CryptoUtils.randomString(32);
        return this;
    }
    
    /**
     * Sets the remember me functionality
     * @deprecated As of release 4.3.0, replaced by {@link #rememberMe(boolean)}, will be removed in 5.0.0
     * 
     * @param remember true or false
     */
    @Deprecated
    public void remember(boolean remember) {
        this.remember = remember;
    }
    
    /**
     * Sets the remember me functionality, default is false
     * 
     * @param rememmber The state of remember to set
     * @return Authentication object
     */
    public Authentication rememberMe(boolean rememmber) {
        this.remember = rememmber;
        return this;
    }
    
    /**
     * Sets the remember me functionality to true, default is false
     * @return Authentication object
     */
    public Authentication rememberMe() {
        this.remember = true;
        return this;
    }
    
    /**
     * Sets the requirement of the two factor authentication, default is false
     * 
     * @param twoFactor True for enabling two factor authentication, false otherwise
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
        Objects.requireNonNull(username, Required.USERNAME.toString());
        boolean lock = false;
        
        AtomicInteger counter = this.cache.getCounter(username);
        if (counter != null && counter.get() > this.config.getAuthenticationLock()) {
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
        Objects.requireNonNull(secret, Required.SECRET.toString());
        Objects.requireNonNull(number, Required.TOTP.toString());
        
        return TotpUtils.verifiedTotp(secret, number, HmacShaAlgorithm.HMAC_SHA_512);
    }

    /**
     * Performs a logout of the currently authenticated user
     */
    public void logout() {
        this.loggedOut = true;
    }

    /**
     * Checks if the authentication contains an authenticated user
     * @deprecated As of version 4.13.0, use isValid() instead
     *
     * @return True if authentication contains an authenticated user, false otherwise
     */
    @Deprecated
    public boolean hasAuthenticatedUser() {
        return StringUtils.isNotBlank(this.identifier) || this.oAuthUser != null;
    }
    
    /**
     * Checks if the authentication class contains an authentication
     *
     * @return True if authentication contains an authentication, false otherwise
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(this.identifier) || this.oAuthUser != null;
    }

    /**
     * Checks if the given user name is authenticated
     * @deprecated  As of release 4.3.0, will be removed in 5.0.0
     *
     * @param username The user name to check
     * @return True if the given user name is authenticates
     */
    @Deprecated
    public boolean isAuthenticated(String username) {
        Objects.requireNonNull(username, Required.USERNAME.toString());

        return username.equals(this.identifier);
    }
}