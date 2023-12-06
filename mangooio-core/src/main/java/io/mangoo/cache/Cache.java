package io.mangoo.cache;

import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public interface Cache {
    /**
     * Adds a value to cache with a given key overwriting and existing value
     *
     * @param key The key for the cached value
     * @param value The value to store
     */
    void put(String key, Object value);
    
    /**
     * Adds a value to cache with a given key overwriting and existing value
     * The value will expire after the given expiration value and the given
     * temporal unit for the expiration
     *
     * @param key The key for the cached value
     * @param value The value to store
     * @param expires The time after which the entry expires
     * @param temporalUnit The time unit for the expiration
     */
    void put(String key, Object value, int expires, TemporalUnit temporalUnit);

    /**
     * Removes a value with a given key from the cache
     *
     * @param key The key for the cached value
     */
    void remove(String key);

    /**
     * Clears the complete cache by invalidating all entries
     */
    void clear();

    /**
     * Retrieves an object from the caches and converts it to
     * a given class
     *
     * @param key The key for the cached value
     * @param <T> JavaDoc requires this (just ignore it)
     *
     * @return A converted cache class value
     */
    <T> T get(String key);

    <T> T get(String key, Function<String, Object> fallback);

    /**
     * Adds a complete map of objects to the cache
     *
     * @param map The map to add
     */
    void putAll(Map<String, Object> map);
    
    /**
     * Increments a cache counter with a given key
     * 
     * If the counter did not exist, it will be
     * created and the incremented
     * 
     * @param key The key for the cached value
     * @return A counter based on AtomicInteger
     */
    AtomicInteger getAndIncrementCounter(String key);
    
    /**
     * Decrements a counter with a given key
     * 
     * If the counter did not exist, it will be
     * created and the decremented
     * 
     * @param key The key for the cached value
     * @return A counter based on AtomicInteger
     */
    AtomicInteger getAndDecrementCounter(String key);

    /**
     * Retrieves the counter for a given key
     * 
     * @param key The key for the counter
     * @return A counter based on AtomicInteger or null if none found
     */
    AtomicInteger getCounter(String key);

    /**
     * Resets the counter for a given key
     * 
     * @param key The key for the counter
     * @return A counter based on AtomicInteger
     */
    AtomicInteger resetCounter(String key);

    /**
     * Retrieves the values of multiple given keys or null if no value found
     * 
     * @param keys The keys to retrieve from cache
     * @return A Map of key and value
     */
    Map<String, Object> getAll(String... keys);
}