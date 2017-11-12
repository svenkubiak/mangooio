package io.mangoo.models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.LongAdder;

import com.google.inject.Singleton;

/**
 * Base class for counting system metrics
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Metrics {
    private static final int CONCURRENCY_LEVEL = 1;
    private static final float LOAD_FACTOR = 0.9F;
    private static final int INITIAL_CAPACITY = 16;
    private AtomicIntegerFieldUpdater<Metrics> maxRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(Metrics.class, "maxRequestTime");
    private AtomicIntegerFieldUpdater<Metrics> minRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(Metrics.class, "minRequestTime");
    private AtomicLongFieldUpdater<Metrics> totalRequestTimeUpdater = AtomicLongFieldUpdater.newUpdater(Metrics.class, "totalRequestTime");
    private AtomicLongFieldUpdater<Metrics> totalRequestsUpdater = AtomicLongFieldUpdater.newUpdater(Metrics.class, "totalRequests");
    private Map<Integer, LongAdder> metricsCount = new ConcurrentHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
    private volatile long avgRequestTime;
    private volatile long totalRequestTime;
    private volatile long totalRequests;
    private volatile int maxRequestTime;
    private volatile int minRequestTime;

    public Metrics() {
        //Empty constructor for Google Guice
    }
    
    public Metrics(Metrics copy) {
        this.totalRequestTime = copy.totalRequestTime;
        this.maxRequestTime = copy.maxRequestTime;
        this.minRequestTime = copy.minRequestTime;
        this.totalRequests = copy.totalRequests;
    }
    
    public void inc(int responseCode) {
        this.metricsCount.computeIfAbsent(responseCode, (Integer integer) -> new LongAdder()).increment();
    }
    
    public void update(final int requestTime) {
        this.totalRequestTimeUpdater.addAndGet(this, requestTime);        
        
        int tempMaxRequestTime;
        do {
            tempMaxRequestTime = this.maxRequestTime;
            if (requestTime < tempMaxRequestTime) {
                break;
            }
        } while (!this.maxRequestTimeUpdater.compareAndSet(this, tempMaxRequestTime, requestTime));

        int tempMinRequestTime;
        do {
            tempMinRequestTime = this.minRequestTime;
            if (requestTime > tempMinRequestTime && tempMinRequestTime != -1) {
                break;
            }
        } while (!this.minRequestTimeUpdater.compareAndSet(this, tempMinRequestTime, requestTime));
        
        this.totalRequestsUpdater.incrementAndGet(this);
        this.avgRequestTime = this.totalRequestTime / this.totalRequests;
    }

    public Map<Integer, LongAdder> getMetrics() {
        return this.metricsCount;
    }

    public int getMaxRequestTime() {
        return maxRequestTime;
    }

    public int getMinRequestTime() {
        return minRequestTime;
    }
    
    public long getAvgRequestTime() {
        return avgRequestTime;
    }

    public void reset() {
        this.maxRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(Metrics.class, "maxRequestTime");
        this.minRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(Metrics.class, "minRequestTime");
        this.totalRequestTimeUpdater = AtomicLongFieldUpdater.newUpdater(Metrics.class, "totalRequestTime");
        this.totalRequestsUpdater = AtomicLongFieldUpdater.newUpdater(Metrics.class, "totalRequests");
        this.metricsCount = new ConcurrentHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
        this.avgRequestTime = 0;
        this.totalRequestTime = 0;
        this.totalRequests = 0;
        this.maxRequestTime = 0;
        this.minRequestTime = 0;
    }
}