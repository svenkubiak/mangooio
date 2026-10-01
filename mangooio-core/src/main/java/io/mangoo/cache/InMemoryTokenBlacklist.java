package io.mangoo.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Ticker;
import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import io.mangoo.interfaces.TokenBlacklist;
import io.mangoo.utils.Argument;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Default TokenBlacklist; every entry lives as long as the revoked token could still be valid, so no revocation expires early.
 * Revocations are local to the running instance and do not survive a restart.
 */
@Singleton
public class InMemoryTokenBlacklist implements TokenBlacklist {
    private static final Logger LOG = LogManager.getLogger(InMemoryTokenBlacklist.class);
    private static final long MAXIMUM_SIZE = 1_000_000;
    private final Cache<String, Instant> tokens;
    private final Cache<String, Instant> subjects;
    private final Duration maxTokenLifetime;
    private final Clock clock;

    @Inject
    public InMemoryTokenBlacklist(Config config) {
        this(maxTokenLifetime(Objects.requireNonNull(config, Required.CONFIG)), Ticker.systemTicker(), Clock.systemUTC());
    }

    InMemoryTokenBlacklist(Duration maxTokenLifetime, Ticker ticker, Clock clock) {
        this.maxTokenLifetime = Objects.requireNonNull(maxTokenLifetime, "maxTokenLifetime can not be null");
        this.clock = Objects.requireNonNull(clock, "clock can not be null");
        Objects.requireNonNull(ticker, "ticker can not be null");

        this.tokens = Caffeine.newBuilder()
                .maximumSize(MAXIMUM_SIZE)
                .ticker(ticker)
                .expireAfter(Expiry.writing((String jwtId, Instant expiresAt) -> remaining(expiresAt)))
                .evictionListener((String jwtId, Instant expiresAt, RemovalCause cause) -> warnOnSizeEviction(cause))
                .build();

        this.subjects = Caffeine.newBuilder()
                .maximumSize(MAXIMUM_SIZE)
                .ticker(ticker)
                .expireAfter(Expiry.writing((String subject, Instant since) -> remaining(since.plus(this.maxTokenLifetime))))
                .evictionListener((String subject, Instant since, RemovalCause cause) -> warnOnSizeEviction(cause))
                .build();
    }

    @Override
    public void revoke(String jwtId, Instant expiresAt) {
        Argument.requireNonBlank(jwtId, Required.ID);
        Objects.requireNonNull(expiresAt, "expiresAt can not be null");

        if (expiresAt.isAfter(clock.instant())) {
            tokens.put(jwtId, expiresAt);
        }
    }

    @Override
    public void revokeSubject(String subject, Instant since) {
        Argument.requireNonBlank(subject, Required.SUBJECT);
        Objects.requireNonNull(since, "since can not be null");

        subjects.asMap().merge(subject, since, (current, update) -> update.isAfter(current) ? update : current);
    }

    @Override
    public boolean isRevoked(String jwtId, String subject, Instant issuedAt) {
        if (StringUtils.isNotBlank(jwtId) && tokens.getIfPresent(jwtId) != null) {
            return true;
        }

        if (StringUtils.isNotBlank(subject) && issuedAt != null) {
            Instant since = subjects.getIfPresent(subject);

            // iat has second precision, so a token issued within the second of the revocation (e.g. a new login) stays valid
            return since != null && issuedAt.isBefore(since.truncatedTo(ChronoUnit.SECONDS));
        }

        return false;
    }

    private Duration remaining(Instant until) {
        var remaining = Duration.between(clock.instant(), until);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    private static void warnOnSizeEviction(RemovalCause cause) {
        if (cause == RemovalCause.SIZE) {
            LOG.warn("Token blacklist reached its maximum size of {} entries, revoked tokens are being evicted before their expiry", MAXIMUM_SIZE);
        }
    }

    private static Duration maxTokenLifetime(Config config) {
        return Duration.ofSeconds(Math.max(config.getAuthenticationCookieRememberExpires(), config.getAuthenticationCookieTokenExpires()));
    }
}
