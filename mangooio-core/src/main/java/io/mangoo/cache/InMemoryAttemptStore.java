package io.mangoo.cache;

import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.cache.Cache;
import io.mangoo.constants.Required;
import io.mangoo.interfaces.AttemptStore;
import io.mangoo.utils.Argument;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Default AttemptStore without persistence; the budgets are local to the running instance and do not survive a restart.
 * An entry lives at least 60 minutes after the last attempt and never shorter than its lock.
 */
@Singleton
public class InMemoryAttemptStore implements AttemptStore {
    private static final Logger LOG = LogManager.getLogger(InMemoryAttemptStore.class);
    private static final long MAXIMUM_SIZE = 1_000_000;
    private static final Duration RETENTION = Duration.ofMinutes(60);
    private final Cache<String, State> states;
    private final Clock clock;

    private record State(int attempts, Instant lockedUntil, Instant expireAt) {
        boolean isLocked(Instant now) {
            return lockedUntil != null && now.isBefore(lockedUntil);
        }
    }

    public InMemoryAttemptStore() {
        this(Ticker.systemTicker(), Clock.systemUTC());
    }

    InMemoryAttemptStore(Ticker ticker, Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock can not be null");
        Objects.requireNonNull(ticker, "ticker can not be null");

        this.states = Caffeine.newBuilder()
                .maximumSize(MAXIMUM_SIZE)
                .ticker(ticker)
                .expireAfter(Expiry.writing((String key, State state) -> remaining(state.expireAt())))
                .evictionListener((String key, State state, RemovalCause cause) -> {
                    if (cause == RemovalCause.SIZE) {
                        LOG.warn("Attempt store reached its maximum size of {} entries, budgets are being evicted before their expiry", MAXIMUM_SIZE);
                    }
                })
                .build();
    }

    @Override
    public boolean tryAcquire(String key, int maxAttempts, Duration lockDuration) {
        Argument.requireNonBlank(key, Required.KEY);
        Objects.requireNonNull(lockDuration, Required.DURATION);

        var granted = new boolean[1];
        states.asMap().compute(key, (k, state) -> {
            Instant now = clock.instant();
            int attempts = state == null ? 0 : state.attempts();
            Instant lockedUntil = state == null ? null : state.lockedUntil();

            // An elapsed lock resets the budget to the full number of allowed attempts
            if (lockedUntil != null && !now.isBefore(lockedUntil)) {
                attempts = 0;
                lockedUntil = null;
            }

            granted[0] = lockedUntil == null;
            if (granted[0]) {
                attempts++;
                if (attempts >= maxAttempts) {
                    lockedUntil = now.plus(lockDuration);
                }
            }

            Instant retention = now.plus(lockDuration.compareTo(RETENTION) > 0 ? lockDuration : RETENTION);
            Instant expireAt = lockedUntil != null && lockedUntil.isAfter(retention) ? lockedUntil : retention;

            return new State(attempts, lockedUntil, expireAt);
        });

        return granted[0];
    }

    @Override
    public void release(String key, int maxAttempts) {
        Argument.requireNonBlank(key, Required.KEY);

        states.asMap().computeIfPresent(key, (k, state) -> {
            int attempts = Math.max(0, state.attempts() - 1);
            if (attempts == 0) {
                return null;
            }

            return new State(attempts, attempts < maxAttempts ? null : state.lockedUntil(), state.expireAt());
        });
    }

    @Override
    public void reset(String key) {
        Argument.requireNonBlank(key, Required.KEY);
        states.invalidate(key);
    }

    @Override
    public boolean isLocked(String key) {
        Argument.requireNonBlank(key, Required.KEY);

        State state = states.getIfPresent(key);
        return state != null && state.isLocked(clock.instant());
    }

    int getAttempts(String key) {
        State state = states.getIfPresent(key);
        return state == null ? 0 : state.attempts();
    }

    Instant getLockedUntil(String key) {
        State state = states.getIfPresent(key);
        return state == null ? null : state.lockedUntil();
    }

    private Duration remaining(Instant until) {
        var remaining = Duration.between(clock.instant(), until);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
}
