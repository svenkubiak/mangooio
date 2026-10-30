package io.mangoo.interfaces;

import java.time.Instant;

public interface OneTimeStore {
    boolean claim(String key, Instant expiresAt);
    boolean advance(String key, long value, Instant expiresAt);
}
