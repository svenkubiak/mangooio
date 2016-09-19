package io.mangoo.cache;

import java.util.Map;
import java.util.Objects;

/**
 * Concrete Cache implementation 
 * 
 * @author sven.kubiak
 *
 */
public class CacheImpl implements Cache {
    private org.ehcache.Cache<String, Object> ehCache;
    
    public CacheImpl(org.ehcache.Cache<String, Object> ehCache) {
        this.ehCache = ehCache;
    }
    
    @Override
    public void put(String key, Object value) {
        Objects.requireNonNull(key, "key can not be null");
        ehCache.put(key, value);
    }

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, "key can not be null");
        ehCache.remove(key);
    }

    @Override
    public void clear() {
        ehCache.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Objects.requireNonNull(key, "key can not be null");
        return (T) ehCache.get(key);
    }

    @Override
    public void putAll(Map<String, Object> map) {
        Objects.requireNonNull(map, "map can not be null");
        ehCache.putAll(map);
    }
}