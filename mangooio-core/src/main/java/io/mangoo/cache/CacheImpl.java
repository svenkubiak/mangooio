package io.mangoo.cache;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.mangoo.enums.Required;

/**
 * EhCache implementation 
 * 
 * @author sven.kubiak
 *
 */
public class CacheImpl implements Cache {
    private org.ehcache.Cache<String, Object> ehCache;
    
    public CacheImpl(org.ehcache.Cache<String, Object> ehCache) {
        Objects.requireNonNull(ehCache, Required.EHCACHE.toString());
        this.ehCache = ehCache;
    }
    
    @Override
    public void put(String key, Object value) {
        Objects.requireNonNull(key, Required.KEY.toString());
        ehCache.put(key, value);
    }

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        ehCache.remove(key);
    }

    @Override
    public void clear() {
        ehCache.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        return (T) ehCache.get(key);
    }

    @Override
    public void putAll(Map<String, Object> map) {
        Objects.requireNonNull(map, Required.MAP.toString());
        ehCache.putAll(map);
    }

    @Override
    public AtomicInteger increment(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        
        AtomicInteger counter = get(key);
        if (counter == null) {
            counter = new AtomicInteger(-1);
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
    public AtomicInteger decrement(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());

        AtomicInteger counter = get(key);
        if (counter == null) {
            counter = new AtomicInteger(1);
        }
        counter.decrementAndGet();
        put(key, counter);
        
        return counter;
    }
}