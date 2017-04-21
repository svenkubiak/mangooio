package io.mangoo.routing.bindings;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.Required;
import io.mangoo.helpers.TwoFactorHelper;
import io.mangoo.models.OAuthUser;
import io.mangoo.providers.CacheProvider;
import io.mangoo.utils.CodecUtils;

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
    private String authenticatedUser;
    private Cache cache;
    private TwoFactorHelper twoFactorHelper;
    private boolean twoFactor;
    private boolean remember;
    private boolean loggedOut;

    @Inject
    public Authentication(CacheProvider cacheProvider, Config config, TwoFactorHelper twoFactorHelper) {
        Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
        Objects.requireNonNull(config, Required.CONFIG.toString());
        Objects.requireNonNull(twoFactorHelper, Required.TWO_FACTOR_HELPER.toString());
        
        this.cache = cacheProvider.getCache(CacheName.AUTH);
        this.config = config;
        this.twoFactorHelper = twoFactorHelper;
    }
    
    public Authentication withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, Required.EXPIRES.toString());
        if (this.expires == null) {
            this.expires = expires;            
        }
        
        return this;
    }
    
    public Authentication withAuthenticatedUser(String authenticatedUser) {
        if (StringUtils.isBlank(this.authenticatedUser)) {
            this.authenticatedUser = authenticatedUser;            
        }
        
        return this;
    }

    /**
     * Retrieves the current authenticated user
     *
     * @return The username of the current authenticated user or null
     */
    public String getAuthenticatedUser() {
        return this.authenticatedUser;
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
     * @deprecated  As of release 4.3.0, replaced by {@link #validLogin(String, String, String)}
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
            this.authenticatedUser = username;
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
     * @param username The username to authenticate
     * @param password The clear text password
     * @param hash The previously hashed password to check
     * @return True if the new hashed password matches the hash, false otherwise
     */
    public boolean validLogin(String username, String password, String hash) {
        Objects.requireNonNull(username, Required.USERNAME.toString());
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        Objects.requireNonNull(hash, Required.HASH.toString());

        boolean authenticated = false;
        if (!userHasLock(username) && CodecUtils.checkJBCrypt(password, hash)) {
            this.authenticatedUser = username;
            authenticated = true;
        } else {
            this.cache.increment(username);
        }

        return authenticated;
    }
    
    /**
     * Sets the remember me functionality
     * @deprecated  As of release 4.3.0, replaced by {@link #rememberMe(boolean)}
     * 
     * @param remember true or false
     */
    @Deprecated
    public void remember(boolean remember) {
        this.remember = remember;
    }
    
    /**
     * Sets the remember me functionality, default is false
     */
    
    /**
     * Sets the remember me functionality, default is false
     * 
     * @param rememmber True for activatin remember me, false otherwise
     */
    public void rememberMe(boolean rememmber) {
        this.remember = rememmber;
    }
    
    /**
     * Sets the requirement of the two factor authentication, default is false
     */
    
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
    public boolean validSecondFactor(String secret, int number) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        
        boolean valid = this.twoFactorHelper.validateCurrentNumber(number, secret);
        if (valid) {
            this.twoFactor = false;
        }
        
        return valid;
    }

    /**
     * Performs a logout of the currently authenticated user
     */
    public void logout() {
        this.loggedOut = true;
    }

    /**
     * Checks if the authentication contains an authenticated user
     *
     * @return True if authentication contains an authenticated user, false otherwise
     */
    public boolean hasAuthenticatedUser() {
        return StringUtils.isNotBlank(this.authenticatedUser) || this.oAuthUser != null;
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

        return username.equals(this.authenticatedUser);
    }
}