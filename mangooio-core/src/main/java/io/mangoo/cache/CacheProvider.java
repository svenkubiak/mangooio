package io.mangoo.cache;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.mangoo.cache.impl.EhCacheImpl;
import io.mangoo.cache.impl.MemcachedImpl;
import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.interfaces.MangooCache;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class CacheProvider implements Provider<MangooCache> {
    private final MangooCache cache;

    @Inject
    public CacheProvider(Config config, Injector injector) {
        Class<? extends MangooCache> cacheClass = null;

        String cache = config.getString(Key.CACHE_TYPE, Default.CACHE_TYPE.toString());
        if (Default.EHCACHE.toString().equals(cache)) {
            cacheClass = EhCacheImpl.class;
        } else if (Default.MEMCACHED.toString().equals(cache)) {
            cacheClass = MemcachedImpl.class;
        } else if (Default.HAZELCAST.toString().equals(cache)) {
            cacheClass = MemcachedImpl.class;
        } else if (Default.REDIS.toString().equals(cache)) {
            cacheClass = MemcachedImpl.class;
        }

        this.cache = injector.getInstance(cacheClass);
    }

    @Override
    public MangooCache get() {
        return cache;
    }
}