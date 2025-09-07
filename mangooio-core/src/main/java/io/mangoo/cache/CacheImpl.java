package io.mangoo.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.mangoo.constants.Required;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class CacheImpl implements Cache {
    private static final String EXPIRES_SUFFIX = "-expires";
    private final com.github.benmanes.caffeine.cache.Cache<String, Object> caffeineCache;
    
    public CacheImpl(com.github.benmanes.caffeine.cache.Cache<String, Object> caffeineCache) {
        Objects.requireNonNull(caffeineCache, Required.CACHE);
        this.caffeineCache = caffeineCache;
    }

    @Override
    public void put(String key, Object value) {
        Objects.requireNonNull(key, Required.KEY);
        caffeineCache.put(key, value);
    }

	@Override
	public void put(String key, Object value, int expires, TemporalUnit temporalUnit) {
		Objects.requireNonNull(key, Required.KEY);
		Objects.requireNonNull(temporalUnit, Required.TEMPORAL_UNIT);

        put(key, value, LocalDateTime.now().plus(expires, temporalUnit));
	}

    @Override
    public void put(String key, Object value, LocalDateTime expires) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(expires, Required.EXPIRES);

        caffeineCache.put(key, value);
        caffeineCache.put(key + EXPIRES_SUFFIX, expires);
    }

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, Required.KEY);
        caffeineCache.invalidate(key);
    }

    @Override
    public void clear() {
        caffeineCache.invalidateAll();
        caffeineCache.cleanUp();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Objects.requireNonNull(key, Required.KEY);
        
        Object expires = caffeineCache.getIfPresent(key + EXPIRES_SUFFIX);
        if (expires != null && LocalDateTime.now().isAfter((LocalDateTime) expires)) {
        	remove(key);
        	remove(key + EXPIRES_SUFFIX);
        }

        return (T) caffeineCache.getIfPresent(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Function<String, Object> fallback) {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(fallback, Required.FALLBACK);

        var object = caffeineCache.getIfPresent(key);
        if (object == null) {
            Map<String, Object> temp = new HashMap<>();
            temp.computeIfAbsent(key, fallback);
            object = temp.get(key);

            caffeineCache.put(key, object);
        }

        return (T) object;
    }

    @Override
    public Map<String, Object> getAll(String... keys) {
        Objects.requireNonNull(keys, Required.KEY);
        
        Map<String, Object> values = HashMap.newHashMap(keys.length + 1);
        for (String key : keys) {
            values.put(key, get(key));
        }

        return values;
    }

    @Override
    public void putAll(Map<String, Object> map) {
        Objects.requireNonNull(map, Required.MAP);
        caffeineCache.putAll(map);
    }

    @Override
    public AtomicInteger getAndIncrementCounter(String key) {
        Objects.requireNonNull(key, Required.KEY);
        
        AtomicInteger counter = get(key);
        if (counter == null) {
            counter = new AtomicInteger(0);
        }
        counter.incrementAndGet();
        put(key, counter);
        
        return counter;
    }
    
    @Override
    public AtomicInteger getCounter(String key) {
        Objects.requireNonNull(key, Required.KEY);
        return get(key);
    }
    
    @Override
    public AtomicInteger resetCounter(String key) {
        Objects.requireNonNull(key, Required.KEY);
        
        AtomicInteger counter = get(key);
        if (counter == null) {
            counter = new AtomicInteger(0);
        }
        put(key, counter);
        
        return counter;
    }

    @Override
    public AtomicInteger getAndDecrementCounter(String key) {
        Objects.requireNonNull(key, Required.KEY);

        AtomicInteger counter = get(key);
        if (counter == null) {
            counter = new AtomicInteger(0);
        }
        counter.decrementAndGet();
        put(key, counter);
        
        return counter;
    }
    
    public CacheStats getStats() {
        return caffeineCache.stats();
    }
}