package io.mangoo.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.cache.Cache;

/**
 * Base class for counting system metrics
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Metrics {
    private static final String MANGOO_METRICS = "MANGOO-METRICS";
    private Cache cache;
    
    @Inject
    public Metrics(Cache cache) {
        Objects.requireNonNull(cache, "Cache can not be null");
        
        this.cache = cache;
    } 
    
    public void inc(int responseCode) {
        Map<Integer, LongAdder> metrics = getMetricsCounter();
        metrics.computeIfAbsent(responseCode, t -> new LongAdder()).increment();
        this.cache.put(MANGOO_METRICS, metrics);
    }

    private Map<Integer, LongAdder> getMetricsCounter() {
        Map<Integer, LongAdder> metricsCounter = this.cache.get(MANGOO_METRICS);
        if (metricsCounter == null) {
            metricsCounter = new HashMap<>();
        }
        
        return metricsCounter;
    }

    public Map<Integer, LongAdder> getMetrics() {
        return getMetricsCounter();
    }
}