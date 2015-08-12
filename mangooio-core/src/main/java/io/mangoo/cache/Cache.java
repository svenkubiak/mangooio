package io.mangoo.cache;

import java.io.NotSerializableException;
import java.io.Serializable;

import com.google.inject.Singleton;

import io.mangoo.enums.Default;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Cache {
    private net.sf.ehcache.Cache cache;

    public Cache() {
        CacheManager cm = CacheManager.getInstance();
        cm.addCacheIfAbsent(Default.CACHE_NAME.toString());
        this.cache = cm.getCache(Default.CACHE_NAME.toString());
    }

    public void add(String key, Object value) {
        check(value);
        this.cache.put(new Element(key, value));
    }

    public void add(String key, Object value, int expiration) {
        check(value);
        Element element = new Element(key, value);
        element.setTimeToLive(expiration);

        this.cache.put(element);
    }

    public Object get(String key) {
        if (this.cache.get(key) != null) {
            return this.cache.get(key).getObjectValue();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (this.cache.get(key) != null) {
            return (T) this.cache.get(key).getObjectValue();
        }

        return null;
    }

    public void clear() {
        this.cache.removeAll();
    }

    private void check(Object value) {
        if (value != null && !(value instanceof Serializable)) {
            new NotSerializableException("Cannot cache a non-serializable value of type " + value.getClass().getName());
        }
    }
}