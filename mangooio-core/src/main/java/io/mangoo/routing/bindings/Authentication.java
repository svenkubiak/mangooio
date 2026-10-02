package io.mangoo.routing.bindings;

import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooHashingException;
import io.mangoo.models.AuthenticationLock;
import io.mangoo.utils.Argument;
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
    // Guards creating, reserving and resetting attempt budgets; the slow verification runs outside of it
    private static final Object ATTEMPTS = new Object();
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

    public String getSubject() {
        return subject;
    }
    
    /**
     * Expires the authentication cookie on the client.
     */
    public void invalidate() {
        invalid = true;
    }

    public LocalDateTime getExpires() {
        return expires;
    }

    public boolean isLogout() {
        return loggedOut;
    }

    public boolean isRememberMe() {
        return remember;
    }
    
    public boolean isTwoFactor() {
        return twoFactor;
    }
    
    /**
     * Throttled per identifier with a budget separate from the second factor.
     * Returns false without counting an attempt if no hashing slot is available in time.
     */
    public boolean isValidLogin(String identifier, String password, String salt, String hash) {
        Objects.requireNonNull(identifier, Required.USERNAME);
        Objects.requireNonNull(password, Required.PASSWORD);
        Argument.requireNonBlank(salt, Required.SALT);
        Objects.requireNonNull(hash, Required.HASH);

        var key = CacheName.AUTH_PASSWORD_PREFIX + identifier;
        var lock = acquire(key);
        if (lock == null) {
            return false;
        }

        try {
            if (CommonUtils.matchArgon2(password, salt, hash)) {
                reset(key);
                return true;
            }
        } catch (MangooHashingException e) {
            release(key, lock);
            LOG.error("Failed to check login credentials", e);
        }

        return false;
    }
    
    /**
     * Does not verify any credentials, isValidLogin must be called before.
     */
    public Authentication login(String subject) {
        this.subject = subject;
        return this;
    }
    
    public Authentication rememberMe(boolean remember) {
        this.remember = remember;
        return this;
    }
    
    public Authentication rememberMe() {
        this.remember = true;
        return this;
    }
    
    public Authentication twoFactorAuthentication(boolean twoFactor) {
        this.twoFactor = twoFactor;
        return this;
    }
 
    /**
     * Only covers the password step, see userHasSecondFactorLock for the second factor.
     */
    public boolean userHasLock(String username) {
        Objects.requireNonNull(username, Required.USERNAME);
        return hasLock(CacheName.AUTH_PASSWORD_PREFIX + username);
    }

    public boolean userHasSecondFactorLock(String identifier) {
        Objects.requireNonNull(identifier, Required.USERNAME);
        return hasLock(CacheName.AUTH_SECOND_FACTOR_PREFIX + identifier);
    }

    /**
     * Unthrottled, so the six-digit TOTP can be brute-forced.
     *
     * @deprecated Use {@link #isValidSecondFactor(String, String, String)} instead
     */
    @Deprecated(since = "10.13.0", forRemoval = false)
    public boolean isValidSecondFactor(String secret, String totp) {
        Objects.requireNonNull(secret, Required.SECRET);
        Objects.requireNonNull(totp, Required.TOTP);

        return TotpUtils.verifyTotp(secret, totp);
    }

    /** Throttled per identifier with a budget separate from the password step. */
    public boolean isValidSecondFactor(String identifier, String secret, String totp) {
        Objects.requireNonNull(identifier, Required.USERNAME);
        Objects.requireNonNull(secret, Required.SECRET);
        Objects.requireNonNull(totp, Required.TOTP);

        var key = CacheName.AUTH_SECOND_FACTOR_PREFIX + identifier;
        if (acquire(key) == null) {
            return false;
        }

        if (TotpUtils.verifyTotp(secret, totp)) {
            reset(key);
            return true;
        }

        return false;
    }

    private boolean hasLock(String key) {
        var cache = Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);
        AuthenticationLock lock = cache.get(key);

        return lock != null && lock.isLocked();
    }

    // Reserves an attempt before verification and returns null if the budget is used up; the put renews the cache expiry
    private static AuthenticationLock acquire(String key) {
        var config = Application.getInstance(Config.class);
        Cache cache = Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);

        synchronized (ATTEMPTS) {
            AuthenticationLock lock = cache.get(key);
            if (lock == null) {
                lock = new AuthenticationLock();
            }

            boolean acquired = lock.tryAcquire(config.getAuthenticationLock(), Duration.ofMinutes(config.getAuthenticationLockDuration()));
            cache.put(key, lock);

            return acquired ? lock : null;
        }
    }

    private static void release(String key, AuthenticationLock lock) {
        synchronized (ATTEMPTS) {
            lock.release(Application.getInstance(Config.class).getAuthenticationLock());
            if (lock.getAttempts() == 0) {
                Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH).remove(key);
            }
        }
    }

    private static void reset(String key) {
        synchronized (ATTEMPTS) {
            Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH).remove(key);
        }
    }

    public void logout() {
        loggedOut = true;
    }

    /**
     * Resends the authentication cookie to the client.
     */
    public void update() {
        update = true;
    }

    /** Also true while a second factor is pending, use isValid for authorization decisions. */
    public boolean hasSubject() {
        return StringUtils.isNotBlank(subject);
    }

    /** False while a second factor is pending, use this for authorization decisions. */
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