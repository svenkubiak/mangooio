package io.mangoo.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
public class CacheProvider implements Provider<Cache> {
    private final Map<String, Cache> caches = new HashMap<>();
    private Cache cache;
    private static final long SIXTY = 60;
    private static final long THIRTY = 30;
    private static final long FORTY_THOUSAND = 40000;

    @Inject
    @SuppressFBWarnings(value = "FII_USE_FUNCTION_IDENTITY", justification = "Required by cache creation function")
    public CacheProvider(Config config) {
        Objects.requireNonNull(config, NotNull.CONFIG);
        
        initApplicationCache();
        initAuthenticationCache();
        setDefaultApplicationCache();
    }

    private void initApplicationCache() {
        Cache applicationCache = new CacheImpl(Caffeine.newBuilder()
                .maximumSize(FORTY_THOUSAND)
                .expireAfterAccess(Duration.of(THIRTY, ChronoUnit.DAYS))
                .recordStats()
                .build());

        caches.put(CacheName.APPLICATION, applicationCache);
    }

    private void initAuthenticationCache() {
        Cache authenticationCache = new CacheImpl( Caffeine.newBuilder()
                .maximumSize(FORTY_THOUSAND)
                .expireAfterAccess(Duration.of(SIXTY, ChronoUnit.MINUTES))
                .recordStats()
                .build());

        caches.put(CacheName.AUTH, authenticationCache);
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
    
    /**˝
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