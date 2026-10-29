package io.mangoo.cache;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.function.Function;

public interface Cache {
    void put(String key, Object value);
    
    void put(String key, Object value, int expires, TemporalUnit temporalUnit);

    void put(String key, Object value, LocalDateTime expires);

    void remove(String key);

    void clear();

    <T> T get(String key);

    /**
     * Calls the fallback if the key is not cached and caches its result under the key; a null result is returned but not cached.
     */
    <T> T get(String key, Function<String, Object> fallback);

    void putAll(Map<String, Object> map);
    
    /**
     * Atomically adds one to the counter and returns the new value; a missing counter starts at 0.
     * Throws IllegalStateException if the key holds a value that is not a counter.
     */
    long increment(String key);

    /**
     * Atomically subtracts one from the counter and returns the new value; a missing counter starts at 0.
     * Throws IllegalStateException if the key holds a value that is not a counter.
     */
    long decrement(String key);

    /** Returns 0 if no counter exists. */
    long getCounter(String key);

    /** Removes the counter, it starts at 0 again. */
    void resetCounter(String key);

    /**
     * Calls the fallback if the key is not cached and caches its result under the key with the given expiry; a null result is returned but not cached.
     */
    <T> T get(String key, int expires, TemporalUnit temporalUnit, Function<String, Object> fallback);

    /**
     * Keys without a cached value are mapped to null.
     */
    Map<String, Object> getAll(String... keys);
}