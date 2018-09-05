package io.mangoo.routing.bindings;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.HmacShaAlgorithm;
import io.mangoo.enums.Required;
import io.mangoo.models.OAuthUser;
import io.mangoo.providers.CacheProvider;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.TotpUtils;

/**
 * Convenient class for handling authentication
 *
 * @author svenkubiak
 *
 */
public class Authentication {
    private LocalDateTime expires;
    private OAuthUser oAuthUser;
    private String identifier;
    private boolean twoFactor;
    private boolean remember;
    private boolean loggedOut;
    private boolean invalid;
    
    public static Authentication create() {
        return new Authentication();
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
     * Invalidates the authentication by sending expiring the client cookie
     */
    public void invalidate() {
        this.invalid = true;
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
     * @param identifier The identifier to authenticate
     * @param password The clear text password
     * @param hash The previously hashed password to check
     * @return True if the new hashed password matches the hash, false otherwise
     */
    public boolean validLogin(String identifier, String password, String hash) {
        Objects.requireNonNull(identifier, Required.USERNAME.toString());
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        Objects.requireNonNull(hash, Required.HASH.toString());

        Cache cache = Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);
        boolean authenticated = false;
        if (!userHasLock(identifier) && CodecUtils.checkJBCrypt(password, hash)) {
            authenticated = true;
        } else {
            cache.increment(identifier);
        }

        return authenticated;
    }
    
    /**
     * Performs a login by setting the authentication to the given identifier
     * Please note, that calling validLogin is mandatory before this call!
     * 
     * It is highly recommended NOT to use a username or anything else than
     * can be related to a user as an identifer!
     * 
     * @param identifier The identifier to use
     * @return Authentication object
     */
    public Authentication login(String identifier) {
        this.identifier = identifier;
        return this;
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
        
        Config config = Application.getInstance(Config.class);
        Cache cache = Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);
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
     * Checks if the authentication class contains an authentication
     *
     * @return True if authentication contains an authentication, false otherwise
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(this.identifier) || this.oAuthUser != null;
    }
    
    public boolean isInvalid() {
        return this.invalid;
    }
}