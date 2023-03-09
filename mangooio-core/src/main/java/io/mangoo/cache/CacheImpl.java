package io.mangoo.cache;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import io.mangoo.enums.Required;

public class CacheImpl implements Cache {
    private static final String EXPIRES_SUFFIX = "-expires";
	private com.google.common.cache.Cache<String, Object> guavaCache = CacheBuilder.newBuilder().build();
    
    public CacheImpl(com.google.common.cache.Cache<String, Object> guavaCache) {
        Objects.requireNonNull(guavaCache, Required.CACHE.toString());
        this.guavaCache = guavaCache;
    }
    
    @Override
    public void put(String key, Object value) {
        Objects.requireNonNull(key, Required.KEY.toString());
        guavaCache.put(key, value);
    }

	@Override
	public void put(String key, Object value, int expires, TemporalUnit temporalUnit) {
		Objects.requireNonNull(key, Required.KEY.toString());
		Objects.requireNonNull(temporalUnit, Required.TEMPORAL_UNIT.toString());
		
		guavaCache.put(key, value);
		guavaCache.put(key + EXPIRES_SUFFIX, LocalDateTime.now().plus(expires, temporalUnit));
	}

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        guavaCache.invalidate(key);
    }

    @Override
    public void clear() {
        guavaCache.invalidateAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        
        Object expires = guavaCache.getIfPresent(key + EXPIRES_SUFFIX);
        if (expires != null && LocalDateTime.now().isAfter((LocalDateTime) expires)) {
        	remove(key);
        	remove(key + EXPIRES_SUFFIX);
        }
        
        return (T) guavaCache.getIfPresent(key);
    }
    
    @Override
    public <T> Optional<T> fetch(String key) {
        return get(key) == null ? Optional.empty() : Optional.of(get(key));
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
        guavaCache.putAll(map);
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
        return guavaCache.stats();
    }
}