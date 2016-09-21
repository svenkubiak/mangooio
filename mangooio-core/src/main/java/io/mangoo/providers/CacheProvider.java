package io.mangoo.providers;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheImpl;
import io.mangoo.configuration.Config;
import io.mangoo.enums.CacheName;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class CacheProvider implements Provider<Cache> {
    private Map<CacheName, Cache> caches = new HashMap<>();
    private CacheManager cacheManager;
    private Cache cache;

    @Inject
    public CacheProvider(Config config) {
        if (config.isClusteredCached()) {
            CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder() 
                    .with(ClusteringServiceConfigurationBuilder.cluster(URI.create(config.getCacheClusterUrl())) 
                    .autoCreate());
            
            this.cacheManager = clusteredCacheManagerBuilder.build(true);
        } else {
            this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(); 
            this.cacheManager.init();             
        }
        
        initializeCaches();
    }
    
    /**
     * Initializes all caches required for applications and internal use
     */
    private void initializeCaches() {
        initApplicationCache();
        initAuthenticationCache();
        initRequestCache();
        initServerEventCache();
        initWebSocketCache();
    }
    
    private void initApplicationCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(20000))
                .build();
        
        org.ehcache.Cache<String, Object> applicationCache = cacheManager.createCache(CacheName.APPLICATION.toString(), configuration);
        
        this.cache = new CacheImpl(applicationCache);
    }

    private void initAuthenticationCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(20000))
                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(60, TimeUnit.MINUTES)))
                .build();
        
        org.ehcache.Cache<String, Object> authCache = cacheManager.createCache(CacheName.AUTH.toString(), configuration);
        
        this.caches.put(CacheName.AUTH, new CacheImpl(authCache));
    }

    private void initRequestCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(40000))
                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(60, TimeUnit.SECONDS)))
                .build();
        
        org.ehcache.Cache<String, Object> requestCache = cacheManager.createCache(CacheName.REQUEST.toString(), configuration);
        
        this.caches.put(CacheName.REQUEST, new CacheImpl(requestCache));
    }

    private void initServerEventCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(20000))
                .withExpiry(Expirations.timeToIdleExpiration(Duration.of(30, TimeUnit.MINUTES)))
                .build();
        
        org.ehcache.Cache<String, Object> sseCache = cacheManager.createCache(CacheName.SSE.toString(), configuration);
        
        this.caches.put(CacheName.SSE, new CacheImpl(sseCache));
    }

    private void initWebSocketCache() {
        CacheConfiguration<String, Object> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class, ResourcePoolsBuilder.heap(20000))
                .withExpiry(Expirations.timeToIdleExpiration(Duration.of(30, TimeUnit.MINUTES)))
                .build();
        
        org.ehcache.Cache<String, Object> wssCache = cacheManager.createCache(CacheName.WSS.toString(), configuration);
        
        this.caches.put(CacheName.WSS, new CacheImpl(wssCache));
    }

    @Override
    public Cache get() {
        return this.cache;
    }
    
    /**
     * Retrieves a cache by its name from the cache pool
     * 
     * @param name The name of the cache
     * @return An Cache instance
     */
    public Cache getCache(CacheName name) {
        return this.caches.get(name);
    }
    
    /**
     * Closes all caches
     */
    public void close() {
        cacheManager.close();
    }
}