package io.mangoo.models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
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
    private final AtomicIntegerFieldUpdater<Metrics> maxRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(Metrics.class, "maxRequestTime");
    private final AtomicIntegerFieldUpdater<Metrics> minRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(Metrics.class, "minRequestTime");
    private final Map<Integer, LongAdder> metricsCount = new ConcurrentHashMap<>(16, 0.9f, 1);
    private volatile int maxRequestTime;
    private volatile int minRequestTime = -1;
    
    public void inc(int responseCode) {
        this.metricsCount.computeIfAbsent(responseCode, t -> new LongAdder()).increment();
    }

    public void update(final int requestTime) {
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
}