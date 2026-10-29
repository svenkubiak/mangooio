package io.mangoo.interfaces;

import java.time.Instant;

/**
 * Single use of a key within a time window. The MongoDB implementation is bound when persistence is enabled and is
 * shared between all instances; bind your own implementation in app.Module to replace it.
 */
public interface OneTimeStore {

    /** True for the first claim of the key, false for every further claim until expiresAt. */
    boolean claim(String key, Instant expiresAt);

    /** True if value is greater than the stored value of the key and is now stored, false otherwise. */
    boolean advance(String key, long value, Instant expiresAt);
}
