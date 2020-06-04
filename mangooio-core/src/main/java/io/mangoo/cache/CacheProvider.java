package io.mangoo.cache;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.statistics.CacheStatistics;
import org.ehcache.core.statistics.DefaultStatisticsService;

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
    private StatisticsService statisticsService = new DefaultStatisticsService();
    private Map<String, Cache> caches = new HashMap<>();
    private CacheManager cacheManager;
    private Cache cache;
    private static final long SIXTY = 60;
    private static final long THIRTY = 30;
    private static final long FORTY_THOUSAND_ELEMENTS = 40000;
    private static final long TWENTY_THOUSAND_ELEMENTS = 20000;

    @Inject
    @SuppressFBWarnings(value = "FII_USE_FUNCTION_IDENTITY", justification = "Required by cache creation function")
    public CacheProvider(Config config) {
        Objects.requireNonNull(config, Required.CONFIG.toString());
        
        if (config.isCacheCluserEnable()) {
            CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder()
                    .using(statisticsService)
                    .with(ClusteringServiceConfigurationBuilder.cluster(URI.create(config.getCacheClusterUrl())) 
                    .autoCreate(b -> b));

            this.cacheManager = clusteredCacheManagerBuilder.build(true);
        } else {
            this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                    .using(statisticsService)
                    .build();
            this.cacheManager.init();
        }
        
        initApplicationCache();
        initAuthenticationCache();
        initRequestCache();
        initResponseCache();
        initServerEventCache();
        initWebSocketCache();
        setDefaultApplicationCache();
    }

    private final void initApplicationCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(TWENTY_THOUSAND_ELEMENTS))
                .build();

        registerCacheConfiguration(CacheName.APPLICATION.toString(), configuration);
    }

    private final void initAuthenticationCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(TWENTY_THOUSAND_ELEMENTS))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(SIXTY, ChronoUnit.MINUTES)))
                .build();

        registerCacheConfiguration(CacheName.AUTH.toString(), configuration);
    }

    private final void initRequestCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(FORTY_THOUSAND_ELEMENTS))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(SIXTY, ChronoUnit.SECONDS)))
                .build();

        registerCacheConfiguration(CacheName.REQUEST.toString(), configuration);
    }
    
    private final void initResponseCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(TWENTY_THOUSAND_ELEMENTS))
                .build();

        registerCacheConfiguration(CacheName.RESPONSE.toString(), configuration);
    }

    private final void initServerEventCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(TWENTY_THOUSAND_ELEMENTS))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(THIRTY, ChronoUnit.MINUTES)))
                .build();

        registerCacheConfiguration(CacheName.SSE.toString(), configuration);
    }

    private final void initWebSocketCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(TWENTY_THOUSAND_ELEMENTS))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(THIRTY, ChronoUnit.MINUTES)))
                .build();

        registerCacheConfiguration(CacheName.WSS.toString(), configuration);
    }
    private final void setDefaultApplicationCache() {
        this.cache = getCache(CacheName.APPLICATION);
    }

    /**
     * Registers a new cache with custom configuration
     * 
     * @param name The name of the cache
     * @param configuration The configuration for the cache to use
     */
    public void registerCacheConfiguration(String name, CacheConfiguration<String, Object> configuration) {
        this.caches.put(name, new CacheImpl(cacheManager.createCache(name, configuration)));
    }
    
    /**
     * Returns a map containing cache names and cache statistics
     * 
     * @return Map of cache statistics
     */
    public Map<String, CacheStatistics> getCacheStatistics() {
        Map<String, CacheStatistics> statistics = new HashMap<>();
        for (Entry<String, Cache> entry : caches.entrySet()) {
            String cacheName = entry.getKey();
            statistics.put(cacheName, statisticsService.getCacheStatistics(cacheName));
        }

        return statistics;
    }

    /**
     * Retrieves a cache by its name from the cache pool
     * 
     * @param name The name of the cache
     * @return An Cache instance
     */
    public Cache getCache(CacheName name) {
        return getCache(name.toString());
    }

    /**
     * Retrieves a cache by its name from the cache pool
     * 
     * @param name The name of the cache
     * @return An Cache instance
     */
    public Cache getCache(String name) {
        return this.caches.get(name);
    }

    /**
     * Closes all caches
     */
    public void close() {
        cacheManager.close();
    }

    @Override
    public Cache get() {
        return this.cache;
    }
}