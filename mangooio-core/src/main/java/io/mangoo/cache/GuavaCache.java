package io.mangoo.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

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
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class GuavaCache implements Cache {
	private static final Logger LOG = LogManager.getLogger(Cache.class);
	private static final Config CONFIG = Application.getConfig();
	private static final String VALUE_REQUIRED = "For a new cache entry a non null value is required";
	private static final String KEY_REQUIRED = "For a new cache entry a non null key is required";
	private final com.google.common.cache.Cache<String, Object> cache;

	public GuavaCache() {
		final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
				.maximumSize(CONFIG.getInt(Key.CACHE_MAX_SIZE, Default.CACHE_MAX_SIZE.toInt()));

		if (CONFIG.getBoolean(Key.APPLICATION_ADMIN_CACHE)) {
			cacheBuilder.recordStats();
		}
		
		this.cache = cacheBuilder.build();
	}

    @Override
	public void put(String key, Object value) {
		Objects.requireNonNull(key, KEY_REQUIRED);
		Objects.requireNonNull(value, VALUE_REQUIRED);

		this.cache.put(key, value);
	}

    @Override
	public void remove(String key) {
		Objects.requireNonNull(key, KEY_REQUIRED);

		this.cache.invalidate(key);
	}

    @Override
	public long size() {
		return this.cache.size();
	}

    @Override
	public void clear() {
		this.cache.invalidateAll();
	}

    @Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		Objects.requireNonNull(key, KEY_REQUIRED);

		final Object object = this.cache.getIfPresent(key);
		return object == null ? null : (T) object;
	}
    
    @Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Callable<? extends Object> callable) {
		Objects.requireNonNull(key, KEY_REQUIRED);
		Objects.requireNonNull(callable, "callable can not be null");

		Object object = this.cache.getIfPresent(key);
		if (object == null) {
			try {
				object = this.cache.get(key, callable);
			} catch (final ExecutionException e) {
				LOG.error("Failed to get Cached value", e);
			}
		}

		return object == null ? null : (T) object;
	}

    @Override
	public void putAll(Map<String, Object> map) {
		Objects.requireNonNull(map, "map can not be null");

		this.cache.putAll(map);
	}

    @Override
	public ConcurrentMap<String, Object> getAll() {
		return this.cache.asMap();
	}

    @Override
	public Map<String, Object> getStats() {
		CacheStats cacheStats = this.cache.stats();

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