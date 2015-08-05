package io.mangoo.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Cache {
    private MangooCache mangooCache;

    @Inject
    public Cache(MangooCache mangooCache) {
        this.mangooCache = mangooCache;
    }

    public void add(String key, Object value) {
        mangooCache.add(key, value);
    }

    public void add(String key, Object value, int expiration) {
        mangooCache.add(key, value);
    }

    public Object get(String key) {
        return mangooCache.get(key);
    }

    @SuppressWarnings("all")
    public <T> T get(String key, Class<T> clazz) {
        return mangooCache.get(key, clazz);
    }

    public void clear() {
        mangooCache.clear();
    }
}