package io.mangoo.cache;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Cache {
    private static final Logger LOG = LoggerFactory.getLogger(Cache.class);
    private com.google.common.cache.Cache<String, Object> localCache;

    @Inject
    public Cache(Config config) {
        this.localCache = CacheBuilder.newBuilder()
                .maximumSize(config.getInt(Key.CACHE_MAX_SIZE, Default.CACHE_MAX_SIZE.toInt()))
                .expireAfterAccess(config.getInt(Key.CACHE_EXPIRES, Default.CACHE_EXPIRES.toInt()), TimeUnit.SECONDS)
                .build();
    }

    /**
     * Adds a value to cache with a given key
     *
     * @param key The key for the cached value
     * @param value The value to store
     */
    public void add(String key, Object value) {
        check(value);
        this.localCache.put(key, value);
    }

    /**
     * Adds a value to cache with an expiration time in seconds
     *
     * @param key The key for the cached value
     * @param value The value to store
     * @param expiration The time of expiration in seconds
     *
     * @deprecated Eviction of single cache elements has been
     * removed, use {@link #add(String key, Object value)} instead.
     */
    @Deprecated
    public void add(String key, Object value, int expiration) {
        check(value);
        add(key, value);
    }

    /**
     * Retrieves a value with a given key from the cache. If the value
     *  is not found the callable will be called to retrieve the value
     *
     * @param key The key for the cached value
     * @param callable The callable to invoke when the value is not found
     *
     * @return The stored value or null if not present
     */
    public Object get(String key, Callable<? extends Object> callable) {
        Object object = null;
        try {
            object = this.localCache.get(key, callable);
        } catch (ExecutionException e) {
            LOG.error("Failed to get Cached value", e);
        }

        return object;
    }

    /**
     * Retrieves a value with a given key from the cache
     *
     * @param key The key for the cached value
     * @return The stored value or null if not present
     */
    public Object get(String key) {
        return this.localCache.getIfPresent(key);
    }

    /**
     * Removes a value with a given key from the cache
     *
     * @param key The key for the cached value
     */
    public void remove(String key) {
        this.localCache.invalidate(key);
    }

    /**
     * Returns the size (number of elements) of cached values
     *
     * @return Cache size
     */
    public long size() {
        return this.localCache.size();
    }

    /**
     * Clears the complete cache by invalidating all entries
     */
    public void clear() {
        this.localCache.invalidateAll();
    }

    /**
     * Retrieves an object from the caches and converts it to
     * a given class
     *
     * @param key The key for the cached value
     * @param clazz The class to convert the value to
     * @return A converted cache class value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object object = null;
        if (this.localCache.getIfPresent(key) != null) {
            object = this.localCache.getIfPresent(key);
        }

        return (T) object;
    }

    /**
     * Retrieves an object from the caches and converts it to
     * a given class. If the value is not found the callable
     * will be called to retrieve the value.
     *
     * @param key The key for the cached value
     * @param clazz The class to convert the value to
     * @param callable The callable to invoke when the value is not found
     * @return A converted cache class value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz, Callable<? extends Object> callable) {
        Object object = null;
        if (this.localCache.getIfPresent(key) != null) {
            try {
                object = this.localCache.get(key, callable);
            } catch (ExecutionException e) {
                LOG.error("Failed to get Cached value", e);
            }
        }

        return (T) object;
    }

    /**
     * Check if a value is serializable and ready to store in the cache
     *
     * @param value The value to check
     */
    private void check(Object value) {
        if (value != null && !(value instanceof Serializable)) {
            try {
                throw new NotSerializableException("Cannot cache a non-serializable value of type " + value.getClass().getName());
            } catch (NotSerializableException e) {
                LOG.error("Failed to check serialization for caching", e);
            }
        }
    }
}