package io.mangoo.models;

import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Singleton
public class Metrics {
    private static final int CONCURRENCY_LEVEL = 1;
    private static final float LOAD_FACTOR = 0.9F;
    private static final int INITIAL_CAPACITY = 16;
    private final Map<Integer, LongAdder> responseCount = new ConcurrentHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
    private final AtomicLong dataSend = new AtomicLong();
    private final AtomicLong totalRequestTime = new AtomicLong();
    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicInteger maxRequestTime = new AtomicInteger(-1);
    private final AtomicInteger minRequestTime = new AtomicInteger(-1);

    public Metrics() {
        // Empty constructor for Google Guice
    }

    public void addStatusCode(int responseCode) {
        responseCount
                .computeIfAbsent(responseCode, key -> new LongAdder())
                .increment();
    }

    public void update(final int requestTime) {
        totalRequestTime.addAndGet(requestTime);
        maxRequestTime.updateAndGet(current -> Math.max(current, requestTime));

        if (requestTime > 0) {
            minRequestTime.updateAndGet(current ->
                    (current == -1) ? requestTime : Math.min(current, requestTime));
        }

        totalRequests.incrementAndGet();
    }

    public Map<Integer, LongAdder> getResponseMetrics() {
        return responseCount;
    }

    public int getMaxRequestTime() {
        return maxRequestTime.get();
    }

    public int getMinRequestTime() {
        return minRequestTime.get();
    }

    public long getAvgRequestTime() {
        long requests = totalRequests.get();
        return (requests == 0) ? 0 : totalRequestTime.get() / requests;
    }

    public void incrementDataSend(long length) {
        dataSend.addAndGet(length);
    }

    public long getDataSend() {
        return dataSend.get();
    }

    public void reset() {
        responseCount.clear();
        dataSend.set(0);
        totalRequestTime.set(0);
        totalRequests.set(0);
        maxRequestTime.set(-1);
        minRequestTime.set(-1);
    }
}