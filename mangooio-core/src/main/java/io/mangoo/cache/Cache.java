package io.mangoo.cache;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author svenkubiak
 *
 */
public interface Cache {
    /**
     * Adds a value to cache with a given key overwriting and existing value
     *
     * @param key The key for the cached value
     * @param value The value to store
     */
    void put(String key, Object value);

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

    /**
     * Adds a complete map of objects to the cache
     *
     * @param map The map to add
     */
    void putAll(Map<String, Object> map);
    
    /**
     * Increments a cache counter with a given key
     * 
     * @param key The key for the cached value
     * @return A counter based on AtomicInteger
     */
    AtomicInteger increment(String key);
    
    /**
     * Decrements a cache counter with a given key
     * 
     * @param key The key for the cached value
     * @return A counter based on AtomicInteger
     */
    AtomicInteger decrement(String key);

    /**
     * Retrieves the current counter for a given key
     * 
     * @param key The key for the cached value
     * @return A counter based on AtomicInteger or null if none found
     */
    AtomicInteger getCounter(String key);
}