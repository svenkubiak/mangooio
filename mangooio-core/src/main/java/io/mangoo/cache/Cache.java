package io.mangoo.cache;

import java.io.NotSerializableException;
import java.io.Serializable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.interfaces.MangooCache;

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
        check(value);
        mangooCache.add(key, value);
    }

    public void add(String key, Object value, int expiration) {
        check(value);
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

    private void check(Object value) {
        if (value != null && !(value instanceof Serializable)) {
            new NotSerializableException("Cannot cache a non-serializable value of type " + value.getClass().getName());
        }
    }
}