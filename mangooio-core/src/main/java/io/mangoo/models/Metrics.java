package io.mangoo.models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.LongAdder;

import com.google.inject.Singleton;

/**
 * Base class for system metrics
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
    private Map<Integer, LongAdder> responseCount = new ConcurrentHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
    private AtomicLong dataSend = new AtomicLong();
    private volatile long avgRequestTime;
    private volatile long totalRequestTime;
    private volatile long totalRequests;
    private volatile int maxRequestTime = -1;
    private volatile int minRequestTime = -1;

    public Metrics() {
        //Empty constructor for Google Guice
    }
    
    public Metrics(Metrics copy) {
        this.totalRequestTime = copy.totalRequestTime;
        this.maxRequestTime = copy.maxRequestTime;
        this.minRequestTime = copy.minRequestTime;
        this.totalRequests = copy.totalRequests;
    }
    
    public void addStatusCode(int responseCode) {
        responseCount.computeIfAbsent(responseCode, (Integer integer) -> new LongAdder()).increment();
    }
    
    public void update(final int requestTime) {
        totalRequestTimeUpdater.addAndGet(this, requestTime);        
        
        int tempMaxRequestTime;
        do {
            tempMaxRequestTime = maxRequestTime;
            if (requestTime < tempMaxRequestTime) {
                break;
            }
        } while (!maxRequestTimeUpdater.compareAndSet(this, tempMaxRequestTime, requestTime));

        if (requestTime > 0) {
            int tempMinRequestTime;
            do {
                tempMinRequestTime = minRequestTime;
                if (requestTime > tempMinRequestTime && tempMinRequestTime != -1) {
                    break;
                }
            } while (!minRequestTimeUpdater.compareAndSet(this, tempMinRequestTime, requestTime)); 
        }
        
        totalRequestsUpdater.incrementAndGet(this);
        avgRequestTime = totalRequestTime / totalRequests;
    }

    public Map<Integer, LongAdder> getResponseMetrics() {
        return responseCount;
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

    public void incrementDataSend(long length) {
        dataSend.addAndGet(length);
    }
    
    public long getDataSend() {
        return dataSend.longValue();
    }

    public void reset() {
        maxRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(Metrics.class, "maxRequestTime");
        minRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(Metrics.class, "minRequestTime");
        totalRequestTimeUpdater = AtomicLongFieldUpdater.newUpdater(Metrics.class, "totalRequestTime");
        totalRequestsUpdater = AtomicLongFieldUpdater.newUpdater(Metrics.class, "totalRequests");
        responseCount = new ConcurrentHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
        dataSend = new AtomicLong();
        avgRequestTime = 0;
        totalRequestTime = 0;
        totalRequests = 0;
        maxRequestTime = -1;
        minRequestTime = -1;
    }
}