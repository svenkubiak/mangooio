package io.mangoo.models;

import io.mangoo.constants.Required;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

// The lock is set once when the failed attempt budget is used up and never extended,
// so an attacker cannot keep the owner locked out indefinitely by sending more failed attempts
public class AuthenticationLock implements Serializable {
    @Serial
    private static final long serialVersionUID = 4172290384766153744L;
    private int attempts;
    private LocalDateTime lockedUntil;

    // Side effect: an elapsed lock resets the budget to the full number of allowed attempts
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

    public synchronized int getAttempts() {
        return attempts;
    }

    public synchronized LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
}
