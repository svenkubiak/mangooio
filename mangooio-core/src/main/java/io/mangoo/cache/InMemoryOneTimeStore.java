package io.mangoo.cache;

import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.cache.Cache;
import io.mangoo.constants.Required;
import io.mangoo.interfaces.OneTimeStore;
import io.mangoo.utils.Argument;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Default OneTimeStore without persistence; claims are local to the running instance and do not survive a restart. */
@Singleton
public class InMemoryOneTimeStore implements OneTimeStore {
    private static final Logger LOG = LogManager.getLogger(InMemoryOneTimeStore.class);
    private static final long MAXIMUM_SIZE = 1_000_000;
    private final Cache<String, Entry> entries;
    private final Clock clock;

    private record Entry(long value, Instant expiresAt) {
    }

    public InMemoryOneTimeStore() {
        this(Ticker.systemTicker(), Clock.systemUTC());
    }

    InMemoryOneTimeStore(Ticker ticker, Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock can not be null");
        Objects.requireNonNull(ticker, "ticker can not be null");

        this.entries = Caffeine.newBuilder()
                .maximumSize(MAXIMUM_SIZE)
                .ticker(ticker)
                .expireAfter(Expiry.writing((String key, Entry entry) -> remaining(entry.expiresAt())))
                .evictionListener((String key, Entry entry, RemovalCause cause) -> {
                    if (cause == RemovalCause.SIZE) {
                        LOG.warn("One-time store reached its maximum size of {} entries, claims are being evicted before their expiry", MAXIMUM_SIZE);
                    }
                })
                .build();
    }

    @Override
    public boolean claim(String key, Instant expiresAt) {
        Argument.requireNonBlank(key, Required.KEY);
        Objects.requireNonNull(expiresAt, "expiresAt can not be null");

        return entries.asMap().putIfAbsent(key, new Entry(0, expiresAt)) == null;
    }

    @Override
    public boolean advance(String key, long value, Instant expiresAt) {
        Argument.requireNonBlank(key, Required.KEY);
        Objects.requireNonNull(expiresAt, "expiresAt can not be null");

        var advanced = new boolean[1];
        entries.asMap().compute(key, (k, entry) -> {
            advanced[0] = entry == null || entry.value() < value;
            return advanced[0] ? new Entry(value, expiresAt) : entry;
        });

        return advanced[0];
    }

    private Duration remaining(Instant until) {
        var remaining = Duration.between(clock.instant(), until);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
}
