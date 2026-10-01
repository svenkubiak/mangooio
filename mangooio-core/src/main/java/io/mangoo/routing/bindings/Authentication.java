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
     * Throttled per identifier: after authentication.lock failed attempts every call returns false for authentication.lock.duration minutes, with a budget separate from the second factor.
     * Fails closed with false, without counting an attempt, if no Argon2 hashing slot is available within authentication.hashing.timeout.
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

    /**
     * Throttled per identifier: after authentication.lock failed attempts every call returns false for authentication.lock.duration minutes, with a budget separate from the password step.
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

    public void logout() {
        loggedOut = true;
    }

    /**
     * Resends the authentication cookie to the client.
     */
    public void update() {
        update = true;
    }

    /**
     * True as soon as the password step succeeded, even if a second factor is still pending; never use it for authorization decisions, use isValid instead.
     */
    public boolean hasSubject() {
        return StringUtils.isNotBlank(subject);
    }

    /**
     * Use this for authorization decisions, as it is false while a required second factor is pending even though getSubject already returns the subject.
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