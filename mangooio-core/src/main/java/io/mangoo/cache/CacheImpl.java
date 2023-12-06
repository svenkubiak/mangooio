package io.mangoo.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.mangoo.enums.Required;

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
        Objects.requireNonNull(caffeineCache, Required.CACHE.toString());
        this.caffeineCache = caffeineCache;
    }

    @Override
    public void put(String key, Object value) {
        Objects.requireNonNull(key, Required.KEY.toString());
        caffeineCache.put(key, value);
    }

	@Override
	public void put(String key, Object value, int expires, TemporalUnit temporalUnit) {
		Objects.requireNonNull(key, Required.KEY.toString());
		Objects.requireNonNull(temporalUnit, Required.TEMPORAL_UNIT.toString());

        caffeineCache.put(key, value);
        caffeineCache.put(key + EXPIRES_SUFFIX, LocalDateTime.now().plus(expires, temporalUnit));
	}

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
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
        Objects.requireNonNull(key, Required.KEY.toString());
        
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
        Objects.requireNonNull(key, Required.KEY.toString());
        Objects.requireNonNull(fallback, Required.FALLBACK.toString());

        Object object = caffeineCache.getIfPresent(key);
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
        Objects.requireNonNull(keys, Required.KEY.toString());
        
        Map<String, Object> values = new HashMap<>();
        for (String key : keys) {
            values.put(key, get(key));
        }

        return values;
    }

    @Override
    public void putAll(Map<String, Object> map) {
        Objects.requireNonNull(map, Required.MAP.toString());
        caffeineCache.putAll(map);
    }

    @Override
    public AtomicInteger getAndIncrementCounter(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        
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
        Objects.requireNonNull(key, Required.KEY.toString());
        return get(key);
    }
    
    @Override
    public AtomicInteger resetCounter(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        
        AtomicInteger counter = get(key);
        if (counter == null) {
            counter = new AtomicInteger(0);
        }
        put(key, counter);
        
        return counter;
    }

    @Override
    public AtomicInteger getAndDecrementCounter(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());

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