package io.mangoo.cache.impl;

import com.google.inject.Singleton;

import io.mangoo.enums.Default;
import io.mangoo.interfaces.MangooCache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class EhCacheImpl implements MangooCache {
    private Cache cache;

    public EhCacheImpl() {
        CacheManager cm = CacheManager.getInstance();
        cm.addCacheIfAbsent(Default.CACHE_NAME.toString());
        this.cache = cm.getCache(Default.CACHE_NAME.toString());
    }

    @Override
    public void add(String key, Object value) {
        this.cache.put(new Element(key, value));
    }

    @Override
    public void add(String key, Object value, int expiration) {
        Element element = new Element(key, value);
        element.setTimeToLive(expiration);

        this.cache.put(element);
    }

    @Override
    public Object get(String key) {
        if (this.cache.get(key) != null) {
            return this.cache.get(key).getObjectValue();
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (this.cache.get(key) != null) {
            return (T) this.cache.get(key).getObjectValue();
        }

        return null;
    }

    @Override
    public void clear() {
        this.cache.removeAll();
    }
}