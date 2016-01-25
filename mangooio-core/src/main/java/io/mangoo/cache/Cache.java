package io.mangoo.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
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
    private static final Config CONFIG = Application.getConfig();
    private static final String VALUE_REQUIRED = "For a new cache entry a non null value is required";
    private static final String KEY_REQUIRED = "For a new cache entry a non null key is required";
    private final com.google.common.cache.Cache<String, Object> guavaCache;

    public Cache() {
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder
                .newBuilder()
                .maximumSize(CONFIG.getInt(Key.CACHE_MAX_SIZE, Default.CACHE_MAX_SIZE.toInt()));
        
        String cacheEviction = CONFIG.getString(Key.CACHE_EVICTION, Default.CACHE_EXPIRES.toString());
        int cacheExpires = CONFIG.getInt(Key.CACHE_EXPIRES, Default.CACHE_EXPIRES_ACCESS.toInt());
        
        if (("afterAccess").equalsIgnoreCase(cacheEviction)) {
            cacheBuilder.expireAfterAccess(cacheExpires, TimeUnit.SECONDS);
        } else if (("afterWrite").equalsIgnoreCase(cacheEviction)) {
            cacheBuilder.expireAfterWrite(cacheExpires, TimeUnit.SECONDS);
        } else {
            cacheBuilder.expireAfterAccess(cacheExpires, TimeUnit.SECONDS);
        }

        if (CONFIG.getBoolean(Key.APPLICATION_ADMIN_CACHE)) {
            cacheBuilder.recordStats();
        }

        this.guavaCache = cacheBuilder.build();
    }

    /**
     * Adds a value to cache with a given key overwriting and existing value
     *
     * @param key The key for the cached value
     * @param value The value to store
     */
    public void put(String key, Object value) {
        Objects.requireNonNull(key, KEY_REQUIRED);
        Objects.requireNonNull(value, VALUE_REQUIRED);
        
        this.guavaCache.put(key, value);
    }

    /**
     * Removes a value with a given key from the cache
     *
     * @param key The key for the cached value
     */
    public void remove(String key) {
        Objects.requireNonNull(key, KEY_REQUIRED);

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
     * @param <T> JavaDoc requires this (just ignore it)
     *
     * @return A converted cache class value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Objects.requireNonNull(key, KEY_REQUIRED);

        final Object object = this.guavaCache.getIfPresent(key);
        return object == null ? null : (T) object;
    }

    /**
     * Retrieves an object from the caches and converts it to
     * a given class. If the value is not found the callable
     * will be called to retrieve the value.
     *
     * @param key The key for the cached value
     * @param callable The callable to invoke when the value is not found
     * @param <T> JavaDoc requires this (just ignore it)
     *
     * @return A converted cache class value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Callable<? extends Object> callable) {
        Objects.requireNonNull(key, KEY_REQUIRED);
        Objects.requireNonNull(callable,  "callable can not be null");

        Object object = this.guavaCache.getIfPresent(key);
        if (object == null) {
            try {
                object = this.guavaCache.get(key, callable);
            } catch (final ExecutionException e) {
                LOG.error("Failed to get Cached value", e);
            }
        }

        return object == null ? null : (T) object;
    }

    /**
     * Adds a complete map of objects to the cache
     *
     * @param map The map to add
     */
    public void putAll(Map<String, Object> map) {
        Objects.requireNonNull(map, "map can not be null");

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
     * @return Map containing cache statistics
     */
    public Map<String, Object> getStats () {
        CacheStats cacheStats = this.guavaCache.stats();
        
        Map<String, Object> data = new HashMap<>();
        data.put("Average load penalty", cacheStats.averageLoadPenalty());
        data.put("Eviction count", cacheStats.evictionCount());
        data.put("Hit count", cacheStats.hitCount());
        data.put("Hit rate", cacheStats.hitRate());
        data.put("Load count", cacheStats.loadCount());
        data.put("Load exception count", cacheStats.loadExceptionCount());
        data.put("Load exception rate", cacheStats.loadExceptionRate());
        data.put("Load success rate", cacheStats.loadSuccessCount());
        data.put("Miss count", cacheStats.missCount());
        data.put("Request count", cacheStats.requestCount());
        data.put("Total load time in ns", cacheStats.totalLoadTime());
        
        return data;
    }
}