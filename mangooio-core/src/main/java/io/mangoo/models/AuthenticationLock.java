package io.mangoo.models;

import io.mangoo.constants.Required;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Failed attempt budget for a single authentication step of a single identifier.
 * <p>
 * The lock is stored as an absolute point in time. It is set once, when the budget
 * of allowed failed attempts is used up, and is never extended by additional failed
 * attempts afterwards. This prevents an attacker from keeping the rightful owner of
 * an account locked out indefinitely by simply continuing to send failed attempts.
 */
public class AuthenticationLock implements Serializable {
    @Serial
    private static final long serialVersionUID = 4172290384766153744L;
    private int attempts;
    private LocalDateTime lockedUntil;

    /**
     * Checks if this lock is currently active
     * <p>
     * If a previously set lock has elapsed, the budget is reset, so that the
     * identifier starts over with the full number of allowed failed attempts
     *
     * @return True if the identifier is currently locked, false otherwise
     */
    public synchronized boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }

        if (LocalDateTime.now().isBefore(lockedUntil)) {
            return true;
        }

        attempts = 0;
        lockedUntil = null;

        return false;
    }

    /**
     * Counts a failed attempt and sets an absolute unlock timestamp once the
     * given number of allowed failed attempts is reached
     * <p>
     * An already set unlock timestamp is never moved into the future
     *
     * @param maxAttempts The number of failed attempts after which the identifier is locked
     * @param lockDuration The duration of the lock
     */
    public synchronized void increment(int maxAttempts, Duration lockDuration) {
        Objects.requireNonNull(lockDuration, Required.DURATION);

        if (isLocked()) {
            return;
        }

        attempts++;
        if (attempts >= maxAttempts) {
            lockedUntil = LocalDateTime.now().plus(lockDuration);
        }
    }

    /**
     * @return The number of failed attempts counted so far
     */
    public synchronized int getAttempts() {
        return attempts;
    }

    /**
     * @return The absolute point in time when the lock is released or null if not locked
     */
    public synchronized LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
}
