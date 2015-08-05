package io.mangoo.cache;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.mangoo.cache.impl.EhCacheImpl;
import io.mangoo.cache.impl.MemcachedImpl;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class CacheProvider implements Provider<Cache> {
    private final Cache cache;

    @Inject
    public CacheProvider(Config config) {
        Class<? extends MangooCache> cacheClass = null;

        String cache = config.getString(Key.CACHE_CLASS, Default.CACHE_CLASS.toString());
        if (Default.EHCACHE.toString().equals(cache)) {
            cacheClass = EhCacheImpl.class;
        } else if (Default.MEMCACHED.toString().equals(cache)) {
            cacheClass = MemcachedImpl.class;
        }

        this.cache = (Cache) Application.getInjector().getInstance(cacheClass);
    }

    @Override
    public Cache get() {
        return cache;
    }
}