package io.mangoo.cache;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
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
        this.ehCache.put(key, value);
    }

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        this.ehCache.remove(key);
    }

    @Override
    public void clear() {
        this.ehCache.clear();
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
        this.ehCache.putAll(map);
    }

    @Override
    public AtomicInteger getAndIncrement(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());
        
        AtomicInteger counter = get(key);
        if (counter == null) {
            counter = new AtomicInteger(0);
        }
        counter.incrementAndGet();
        this.put(key, counter);
        
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
        this.put(key, counter);
        
        return counter;
    }

    @Override
    public AtomicInteger getAndDecrement(String key) {
        Objects.requireNonNull(key, Required.KEY.toString());

        AtomicInteger counter = get(key);
        if (counter == null) {
            counter = new AtomicInteger(0);
        }
        counter.decrementAndGet();
        this.put(key, counter);
        
        return counter;
    }
}