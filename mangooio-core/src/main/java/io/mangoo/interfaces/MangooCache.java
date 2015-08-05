package io.mangoo.interfaces;

/**
 *
 * @author svenkubiak
 *
 */
public interface MangooCache {

    /**
     * Adds a value with the given key to the cache
     *
     * @param key The key to store the value
     * @param value The actual value to store
     */
    void add(String key, Object value);

    /**
     * Adds a value with the given key to the cache and
     * sets and expiration
     *
     * @param key The key to store the value
     * @param value The actual value to store
     * @param expiration The time after which the value gets evicted in seconds
     */
    void add(String key, Object value, int expiration);

    /**
     * Retrieves a value for a given key from the cache
     *
     * @param key The key on which the value is stored
     * @return The retrieved value or null if the key is not found
     */
    Object get(String key);

    /**
     * Retrieves a value for given key from the cache
     * auto casting it to the required type
     *
     * @param key The key on which the value is stored
     * @param clazz The class to cast to
     * @return The class to cast to to containing the cache value or null if the key is not found
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Clears the complete cache by removing all entries
     */
    void clear();
}