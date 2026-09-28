package io.mangoo.routing.bindings;

import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooHashingException;
import io.mangoo.models.AuthenticationLock;
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.TotpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Authentication {
    private static final Logger LOG = LogManager.getLogger(Authentication.class);
    private LocalDateTime expires;
    private String subject;
    private String id;
    private boolean twoFactor;
    private boolean remember;
    private boolean loggedOut;
    private boolean invalid;
    private boolean update;
    
    public static Authentication create() {
        return new Authentication();
    }
    
    public Authentication withExpires(LocalDateTime expires) {
        Objects.requireNonNull(expires, Required.EXPIRES);
        this.expires = expires;

        return this;
    }
    
    public Authentication withSubject(String subject) {
        if (StringUtils.isBlank(this.subject)) {
            this.subject = subject;            
        }
        
        return this;
    }

    public Authentication withId(String id) {
        if (StringUtils.isBlank(this.id)) {
            this.id = id;
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
     * <p>
     * The check is throttled per identifier: after authentication.lock failed
     * attempts the identifier is locked for authentication.lock.duration minutes
     * and every further call returns false without checking the password. A
     * successful check resets the budget. The budget of the password step is kept
     * separately from the budget of the second factor step, see
     * {@link #isValidSecondFactor(String, String, String)}
     * <p>
     * If no Argon2 hashing slot becomes available within
     * authentication.hashing.timeout the check fails closed and returns false. The
     * failed attempt budget is not touched in that case, an overload situation must
     * not lock out a user
     *
     * @param identifier The identifier to authenticate
     * @param password The clear text password
     * @param salt The salt to use for hashing
     * @param hash The previously hashed password to check
     * @return True if the new hashed password matches the hash, false otherwise
     */
    public boolean isValidLogin(String identifier, String password, String salt, String hash) {
        Objects.requireNonNull(identifier, Required.USERNAME);
        Objects.requireNonNull(password, Required.PASSWORD);
        Objects.requireNonNull(password, Required.SALT);
        Objects.requireNonNull(hash, Required.HASH);

        var key = CacheName.AUTH_PASSWORD_PREFIX + identifier;
        if (hasLock(key)) {
            return false;
        }

        var cache = Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);
        var authenticated = false;

        try {
            if (CommonUtils.matchArgon2(password, salt, hash)) {
                authenticated = true;
                cache.remove(key);
            } else {
                increaseFailedAttempts(cache, key);
            }
        } catch (MangooHashingException e) {
            LOG.error("Failed to check login credentials", e);
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
     * <p>
     * This refers to the password step only, see
     * {@link #userHasSecondFactorLock(String)} for the second factor step
     *
     * @param username The username to check
     * @return true if the user has a lock, false otherwise
     */
    public boolean userHasLock(String username) {
        Objects.requireNonNull(username, Required.USERNAME);
        return hasLock(CacheName.AUTH_PASSWORD_PREFIX + username);
    }

    /**
     * Checks if an identifier is locked because of to many failed second factor attempts
     *
     * @param identifier The identifier to check
     * @return true if the identifier has a lock, false otherwise
     */
    public boolean userHasSecondFactorLock(String identifier) {
        Objects.requireNonNull(identifier, Required.USERNAME);
        return hasLock(CacheName.AUTH_SECOND_FACTOR_PREFIX + identifier);
    }

    /**
     * Checks if a given number for 2FA is valid for the given secret
     * <p>
     * This method performs an unthrottled check. As a TOTP has only six digits and
     * is verified without a tolerance window, an unlimited number of attempts makes
     * guessing it feasible. Use {@link #isValidSecondFactor(String, String, String)}
     * instead, which keeps a failed attempt budget per identifier
     *
     * @param secret The plaintext secret to use for checking
     * @param totp The number entered by the user
     * @return True if number is valid, false otherwise
     *
     * @deprecated Use {@link #isValidSecondFactor(String, String, String)} instead
     */
    @Deprecated(since = "10.13.0", forRemoval = false)
    public boolean isValidSecondFactor(String secret, String totp) {
        Objects.requireNonNull(secret, Required.SECRET);
        Objects.requireNonNull(totp, Required.TOTP);

        return TotpUtils.verifyTotp(secret, totp);
    }

    /**
     * Checks if a given number for 2FA is valid for the given secret
     * <p>
     * The check is throttled per identifier: after authentication.lock failed
     * attempts the identifier is locked for authentication.lock.duration minutes
     * and every further call returns false without checking the number. A
     * successful check resets the budget. The budget of the second factor step is
     * kept separately from the budget of the password step, see
     * {@link #isValidLogin(String, String, String, String)}
     *
     * @param identifier The identifier the second factor is checked for
     * @param secret The plaintext secret to use for checking
     * @param totp The number entered by the user
     * @return True if number is valid, false otherwise
     */
    public boolean isValidSecondFactor(String identifier, String secret, String totp) {
        Objects.requireNonNull(identifier, Required.USERNAME);
        Objects.requireNonNull(secret, Required.SECRET);
        Objects.requireNonNull(totp, Required.TOTP);

        var key = CacheName.AUTH_SECOND_FACTOR_PREFIX + identifier;
        if (hasLock(key)) {
            return false;
        }

        var cache = Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);
        var authenticated = false;

        if (TotpUtils.verifyTotp(secret, totp)) {
            authenticated = true;
            cache.remove(key);
        } else {
            increaseFailedAttempts(cache, key);
        }

        return authenticated;
    }

    private boolean hasLock(String key) {
        var cache = Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);
        AuthenticationLock lock = cache.get(key);

        return lock != null && lock.isLocked();
    }

    private void increaseFailedAttempts(Cache cache, String key) {
        var config = Application.getInstance(Config.class);

        AuthenticationLock lock = cache.get(key);
        if (lock == null) {
            lock = new AuthenticationLock();
        }

        lock.increment(config.getAuthenticationLock(), Duration.ofMinutes(config.getAuthenticationLockDuration()));
        cache.put(key, lock);
    }

    /**
     * Performs a logout of the currently authenticated user
     */
    public void logout() {
        loggedOut = true;
    }

    /**
     * Triggers a resend of the authentication cookie to the client
     */
    public void update() {
        update = true;
    }

    /**
     * Checks if this authentication carries a subject, regardless of whether the
     * authentication has been completed
     * <p>
     * A subject is already present after the password step has succeeded, which is
     * also the point at which the authentication cookie is issued. If two-factor
     * authentication is required, the second factor is at that moment still
     * outstanding, see {@link #isTwoFactor()}
     * <p>
     * This method must not be used to decide whether a request is authorized. Use
     * {@link #isValid()} for that, which is only true once every required step has
     * been completed. Use this method only where the incomplete state is the state
     * being worked on, for example on the page that asks for the second factor
     *
     * @return True if a subject is present, false otherwise
     */
    public boolean hasSubject() {
        return StringUtils.isNotBlank(subject);
    }

    /**
     * Checks if this authentication is complete, meaning that a subject is present
     * and no further authentication step is outstanding
     * <p>
     * This is the method to use for authorization decisions. It returns false while
     * a required second factor has not been verified yet, even though a subject is
     * present and {@link #getSubject()} returns it
     * <p>
     * Note that the framework provides an Authentication object on every request,
     * not only on routes bound with withAuthentication(). Code that derives access
     * from {@link #getSubject()} without consulting this method grants access to a
     * user whose second factor is still pending
     *
     * @return True if a subject is present and no second factor is outstanding, false otherwise
     */
    public boolean isValid() {
        return hasSubject() && !isTwoFactor();
    }

    public String getId() {
        return id;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean isUpdate() {
        return update;
    }
}