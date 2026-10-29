package io.mangoo.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import io.mangoo.utils.Argument;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
public class CacheProvider implements Provider<Cache> {
    private static final long THIRTY = 30;
    private static final long FIFTY_THOUSAND = 50000;
    private final Map<String, Cache> caches = new HashMap<>();
    private Cache cache;

    @Inject
    @SuppressFBWarnings(value = "FII_USE_FUNCTION_IDENTITY", justification = "Required by cache creation function")
    public CacheProvider(Config config) {
        Objects.requireNonNull(config, Required.CONFIG);

        initApplicationCache();
        setDefaultApplicationCache();
    }

    private void initApplicationCache() {
        Cache applicationCache = new CacheImpl(Caffeine.newBuilder()
                .maximumSize(FIFTY_THOUSAND)
                .expireAfterWrite(Duration.of(THIRTY, ChronoUnit.DAYS))
                .recordStats()
                .build());

        caches.put(CacheName.APPLICATION, applicationCache);
    }

    private void setDefaultApplicationCache() {
        cache = getCache(CacheName.APPLICATION);
    }

    public Cache getCache(String name) {
        Argument.requireNonBlank(name, Required.NAME);
        return caches.get(name);
    }

    /**
     * Makes the cache available to the admin dashboard; the built-in application cache name cannot be replaced.
     */
    public void addCache(String name, Cache cache) {
        Argument.requireNonBlank(name, Required.NAME);
        Objects.requireNonNull(cache, Required.CACHE);

        if (!CacheName.APPLICATION.equalsIgnoreCase(name)) {
            caches.put(name, cache);
        }
    }
    
    public Map<String, Cache> getCaches() {
        return caches;
    }

    @Override
    public Cache get() {
        return cache;
    }
}