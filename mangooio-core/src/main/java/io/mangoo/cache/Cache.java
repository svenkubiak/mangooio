package io.mangoo.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;

/**
 * Google Guava based cache implementation
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Cache {
    private static final Logger LOG = LogManager.getLogger(Cache.class);
    private static final String VALUE_REQUIRED = "A valid value is required";
    private static final String KEY_REQUIRED = "A valid key is required";
    private final com.google.common.cache.Cache<String, Object> guavaCache;

    @Inject
    public Cache(Config config) {
        Preconditions.checkNotNull(config, "config can not be null");

        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .maximumSize(config.getInt(Key.CACHE_MAX_SIZE, Default.CACHE_MAX_SIZE.toInt()))
                .expireAfterAccess(config.getInt(Key.CACHE_EXPIRES, Default.CACHE_EXPIRES.toInt()), TimeUnit.SECONDS);

        if (config.getBoolean(Key.APPLICATION_ADMIN_CACHE)) {
            cacheBuilder.recordStats();
        }

        this.guavaCache = cacheBuilder.build();
    }

    /**
     * Adds a value to cache with a given key
     *
     * @param key The key for the cached value
     * @param value The value to store
     */
    public void put(String key, Object value) {
        Preconditions.checkNotNull(key, KEY_REQUIRED);
        Preconditions.checkNotNull(value, VALUE_REQUIRED);

        this.guavaCache.put(key, value);
    }

    /**
     * Removes a value with a given key from the cache
     *
     * @param key The key for the cached value
     */
    public void remove(String key) {
        Preconditions.checkNotNull(key, KEY_REQUIRED);

        this.guavaCache.invalidate(key);
    }

    /**
     * Returns the size (number of elements) of cached values
     *
     * @return Cache size
     */
    public long size() {
        return this.guavaCache.size();
    }

    /**
     * Clears the complete cache by invalidating all entries
     */
    public void clear() {
        this.guavaCache.invalidateAll();
    }

    /**
     * Retrieves an object from the caches and converts it to
     * a given class
     *
     * @param key The key for the cached value
     *
     * @return A converted cache class value
     */
    public Optional<Object> get(String key) {
        Preconditions.checkNotNull(key, KEY_REQUIRED);

        final Object object = this.guavaCache.getIfPresent(key);
        return object == null ? Optional.empty() : Optional.of(object);
    }

    /**
     * Retrieves an object from the caches and converts it to
     * a given class. If the value is not found the callable
     * will be called to retrieve the value.
     *
     * @param key The key for the cached value
     * @param callable The callable to invoke when the value is not found
     *
     * @return A converted cache class value
     */
    public Optional<Object> get(String key, Callable<? extends Object> callable) {
        Preconditions.checkNotNull(key, KEY_REQUIRED);
        Preconditions.checkNotNull(callable,  "callable can not be null");

        Object object = this.guavaCache.getIfPresent(key);
        if (object == null) {
            try {
                object = this.guavaCache.get(key, callable);
            } catch (final ExecutionException e) {
                LOG.error("Failed to get Cached value", e);
            }
        }

        return object == null ? Optional.empty() : Optional.of(object);
    }

    /**
     * Adds a complete map of objects to the cache
     *
     * @param map The map to put
     */
    public void putAll(Map<String, Object> map) {
        Preconditions.checkNotNull(map, "map can not be null");

        this.guavaCache.putAll(map);
    }

    /**
     * @return The complete content of the cache
     */
    public ConcurrentMap<String, Object> getAll() {
        return this.guavaCache.asMap();
    }

    /**
     * Retrieves the cache statistics
     *
     * @return CacheStats containing cache statistics
     */
    public CacheStats getStats () {
        return this.guavaCache.stats();
    }
}