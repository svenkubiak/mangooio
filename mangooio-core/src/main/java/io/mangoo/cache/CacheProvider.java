package io.mangoo.cache;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.enums.CacheType;
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
    private static final Logger LOG = LogManager.getLogger(CacheProvider.class);
    private MangooCache mangooCache;
    
    @Inject
    public CacheProvider(Config config, Injector injector) {
        Objects.requireNonNull(config, "config can not be null");
        Objects.requireNonNull(injector, "injector can not be null");
        
        String cacheType = config.getString(Key.CACHE_TYPE, Default.CACHE_TYPE.toString());
        if (("memcache").equalsIgnoreCase(cacheType)) {
            this.mangooCache = injector.getInstance(getCache(CacheType.MEMCACHE));
        } else if (("hazelcast").equalsIgnoreCase(cacheType)) {
            this.mangooCache = injector.getInstance(getCache(CacheType.HAZELCAST));
        } else if (("redis").equalsIgnoreCase(cacheType)) {
            this.mangooCache = injector.getInstance(getCache(CacheType.REDIS)); 
        } else {
            this.mangooCache = injector.getInstance(getCache(CacheType.DEFAULT));
        }
    }

    private Class<? extends MangooCache> getCache(CacheType cacheType) {
        Class<? extends MangooCache> mangooCache;
        try {
            mangooCache = Class.forName(cacheType.getClassName()).asSubclass(MangooCache.class);
            LOG.info("Using {} as implementation for Cache.",  mangooCache);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new RuntimeException("Failed to initialize cache implementation", e);
        }
        
        return mangooCache;
    }
    
    @Override
    public MangooCache get() {
        return this.mangooCache;
    }
}