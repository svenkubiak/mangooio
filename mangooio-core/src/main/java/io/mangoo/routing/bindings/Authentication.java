package io.mangoo.routing.bindings;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.enums.CacheName;
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
    private boolean remember;
    private boolean loggedOut;

    @Inject
    public Authentication(CacheProvider cacheProvider, Config config) {
        Objects.requireNonNull(cacheProvider, "cacheProvider can not be null");
        Objects.requireNonNull(config, "config can not be null");
        this.cache = cacheProvider.getCache(CacheName.AUTH);
        this.config = config;
    }
    
    public Authentication withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, "expires can not be null");
        
        this.expires = expires;
        return this;
    }
    
    public Authentication withAuthenticatedUser(String authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
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
        return expires;
    }

    /**
     * @return True if the user wants to logout, false otherwise
     */
    public boolean isLogout() {
        return loggedOut;
    }

    /**
     *
     * @return True if the user wants to stay logged in, false otherwise
     */
    public boolean isRemember() {
        return remember;
    }

    /**
     * Sets an OAuthUser to the current authentication.
     * Can only be set once!
     *
     * @param oAuthUser An OAuthUser
     */
    public void setOAuthUser(OAuthUser oAuthUser) {
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
     *@param username The username to authenticate
     * @param password The clear text password
     * @param hash The previously hashed password to check
     * @return True if the new hashed password matches the hash, false otherwise
     */
    public boolean login(String username, String password, String hash) {
        Objects.requireNonNull(username, "username can not be null");
        Objects.requireNonNull(password, "password can not be null");
        Objects.requireNonNull(hash, "hash can not be null");

        boolean authenticated = false;
        if (!userHasLock(username) && CodecUtils.checkJBCrypt(password, hash)) {
            this.authenticatedUser = username;
            authenticated = true;
        } else {
            this.cache.increment(username);
        }

        return authenticated;
    }
    
    public void remember(boolean remember) {
        this.remember = remember;
    }
 
    /**
     * Checks if a username is locked because of to many failed login attempts
     * 
     * @param username The username to check
     * @return true if the user has a lock, false otherwise
     */
    public boolean userHasLock(String username) {
        Objects.requireNonNull(username, "username can not be null");
        boolean lock = false;
        
        AtomicInteger counter = this.cache.getCounter(username);
        if (counter != null && counter.get() > this.config.getAuthenticationLock()) {
            lock = true;
        }
        
        return lock;
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
     *
     * @param username The user name to check
     * @return True if the given user name is authenticates
     */
    public boolean isAuthenticated(String username) {
        Objects.requireNonNull(username, "username can not be null");

        return username.equals(this.authenticatedUser);
    }
}