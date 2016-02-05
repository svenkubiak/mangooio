package io.mangoo.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import com.google.inject.Singleton;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.LocalMapStats;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class HazlecastCache implements Cache {
    private static final Config CONFIG = Application.getConfig();
    private HazelcastInstance cache = HazelcastClient.newHazelcastClient();

    public HazlecastCache () {
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress(CONFIG.getCacheAddresses());
        
        this.cache = HazelcastClient.newHazelcastClient(config);
    }

    @Override
    public void put(String key, Object value) {
        Objects.requireNonNull(key, Default.KEY_REQUIRED.toString());
        Objects.requireNonNull(value, Default.VALUE_REQUIRED.toString());
        
        this.cache.getMap(Default.CACHE_NAME.toString()).put(key, value);
    }

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, Default.KEY_REQUIRED.toString());
        
        this.cache.getMap(Default.CACHE_NAME.toString()).remove(key);
    }

    @Override
    public long size() {
        return this.cache.getMap(Default.CACHE_NAME.toString()).size();
    }

    @Override
    public void clear() {
        this.cache.getMap(Default.CACHE_NAME.toString()).clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Objects.requireNonNull(key, Default.KEY_REQUIRED.toString());
        
        return (T) this.cache.getMap(Default.CACHE_NAME.toString()).get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Callable<? extends Object> callable) {
        Objects.requireNonNull(key, Default.KEY_REQUIRED.toString());
        
        return (T) this.cache.getMap(Default.CACHE_NAME.toString()).get(key);
    }

    @Override
    public void putAll(Map<String, Object> map) {
        Objects.requireNonNull(map, "map can not be null");
        
        this.cache.getMap(Default.CACHE_NAME.toString()).putAll(map);
    }

    @Override
    public ConcurrentMap<String, Object> getAll() {
        return this.cache.getMap(Default.CACHE_NAME.toString());
    }

    @Override
    public Map<String, Object> getStats() {
        IMap<Object, Object> map = this.cache.getMap(Default.CACHE_NAME.toString());
        LocalMapStats localMapStats = map.getLocalMapStats();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("Hits", localMapStats.getHits());
        stats.put("Misses", localMapStats.getNearCacheStats().getMisses());
        stats.put("Put operation count", localMapStats.getPutOperationCount());
        stats.put("Remove operation count", localMapStats.getRemoveOperationCount());
        
        return stats;
    }
}