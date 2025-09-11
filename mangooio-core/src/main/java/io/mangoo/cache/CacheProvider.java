package io.mangoo.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import io.mangoo.utils.Arguments;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Singleton
public class CacheProvider implements Provider<Cache> {
    private static final long SIXTY = 60;
    private static final long THIRTY = 30;
    private static final long FORTY_THOUSAND = 40000;
    private static final long TEN_THOUSAND = 10000;
    private final Map<String, Cache> caches = new HashMap<>();
    private Cache cache;

    @Inject
    @SuppressFBWarnings(value = "FII_USE_FUNCTION_IDENTITY", justification = "Required by cache creation function")
    public CacheProvider(Config config) {
        Objects.requireNonNull(config, Required.CONFIG);

        if (config.isAuthenticationBlacklist()) {
            initBlacklistCache();
        }
        initApplicationCache();
        initAuthenticationCache();
        setDefaultApplicationCache();
    }

    private void initApplicationCache() {
        Cache applicationCache = new CacheImpl(Caffeine.newBuilder()
                .maximumSize(FORTY_THOUSAND)
                .expireAfterWrite(Duration.of(THIRTY, ChronoUnit.DAYS))
                .recordStats()
                .build());

        caches.put(CacheName.APPLICATION, applicationCache);
    }

    private void initAuthenticationCache() {
        Cache authenticationCache = new CacheImpl( Caffeine.newBuilder()
                .maximumSize(FORTY_THOUSAND)
                .expireAfterWrite(Duration.of(SIXTY, ChronoUnit.MINUTES))
                .recordStats()
                .build());

        caches.put(CacheName.AUTH, authenticationCache);
    }

    private void initBlacklistCache() {
        Cache authenticationCache = new CacheImpl( Caffeine.newBuilder()
                .maximumSize(TEN_THOUSAND)
                .expireAfterWrite(Duration.of(SIXTY, ChronoUnit.MINUTES))
                .recordStats()
                .build());

        caches.put(CacheName.BLACKLIST, authenticationCache);
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
    public Cache getCache(String name) {
        Arguments.requireNonBlank(name, Required.NAME);
        return caches.get(name);
    }

    /**
     * Adds a cache to the CacheProvider list making it available to
     * the Admin Dashboard (if record stats is enabled)
     *
     * @param name The name of the cache
     * @param cache The cache instance
     */
    public void addCache(String name, Cache cache) {
        Arguments.requireNonBlank(name, Required.NAME);
        Objects.requireNonNull(cache, Required.CACHE);

        if (Stream.of(CacheName.APPLICATION, CacheName.BLACKLIST, CacheName.AUTH)
                .noneMatch(s -> s.equalsIgnoreCase(name))) {
            caches.put(name, cache);
        }
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