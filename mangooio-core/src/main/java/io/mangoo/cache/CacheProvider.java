package io.mangoo.cache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.core.Config;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class CacheProvider implements Provider<Cache> {
    private Map<String, Cache> caches = new HashMap<>();
    private Cache cache;
    private static final long TEN = 10;
    private static final long SIXTY = 60;
    private static final long TWENTY_THOUSAND_ELEMENTS = 20000;

    @Inject
    @SuppressFBWarnings(value = "FII_USE_FUNCTION_IDENTITY", justification = "Required by cache creation function")
    public CacheProvider(Config config) {
        Objects.requireNonNull(config, Required.CONFIG.toString());
        
        initApplicationCache();
        initAuthenticationCache();
        initResponseCache();
        initServerEventCache();
        setDefaultApplicationCache();
    }

    private void initApplicationCache() {
        Cache cache = new CacheImpl(CacheBuilder.newBuilder()
                .maximumSize(TWENTY_THOUSAND_ELEMENTS)
                .build());
        
        caches.put(CacheName.APPLICATION.toString(), cache);
    }

    private void initAuthenticationCache() {
        Cache cache = new CacheImpl(CacheBuilder.newBuilder()
                .maximumSize(TWENTY_THOUSAND_ELEMENTS)
                .expireAfterWrite(Duration.of(SIXTY, ChronoUnit.MINUTES))
                .build());
        
        caches.put(CacheName.AUTH.toString(), cache);
    }
    
    private void initResponseCache() {
        Cache cache = new CacheImpl(CacheBuilder.newBuilder()
                .maximumSize(TWENTY_THOUSAND_ELEMENTS)
                .expireAfterWrite(Duration.of(SIXTY, ChronoUnit.MINUTES))
                .build());
        
        caches.put(CacheName.RESPONSE.toString(), cache);
    }

    private void initServerEventCache() {
        Cache cache = new CacheImpl(CacheBuilder.newBuilder()
                .maximumSize(TWENTY_THOUSAND_ELEMENTS)
                .expireAfterAccess(Duration.of(TEN, ChronoUnit.MINUTES))
                .build());
        
        caches.put(CacheName.SSE.toString(), cache);
    }
    
    private void setDefaultApplicationCache() {
        cache = getCache(CacheName.APPLICATION);
    }
    
    /**
     * Retrieves a cache by its name from the cache pool
     * 
     * @param name The name of the cache
     * @return A Cache instance
     */
    public Cache getCache(CacheName name) {
        return getCache(name.toString());
    }

    /**
     * Retrieves a cache by its name from the cache pool
     * 
     * @param name The name of the cache
     * @return A Cache instance
     */
    public Cache getCache(String name) {
        return caches.get(name);
    }
    
    /**
     * @return Map of all caches
     */
    public Map<String, Cache> getCaches() {
        return caches;
    }

    @Override
    public Cache get() {
        return cache;
    }
}