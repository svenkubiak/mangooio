package io.mangoo.models;

import jakarta.inject.Singleton;

import java.time.Clock;
import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.LongAdder;

@Singleton
public class Metrics {
    private static final int CONCURRENCY_LEVEL = 1;
    private static final float LOAD_FACTOR = 0.9F;
    private static final int INITIAL_CAPACITY = 16;
    private static final int WINDOW_MINUTES = 5;
    private static final int SERVER_ERROR = 500;
    private static final int MAX_ROUTES = 1000;
    private static final int MAX_FAILURES = 50;
    private static final String OTHER_ROUTES = "Other routes";
    private final Map<Integer, LongAdder> responseCount = new ConcurrentHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
    private final AtomicLong dataSend = new AtomicLong();
    private final AtomicLong totalRequestTime = new AtomicLong();
    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicInteger maxRequestTime = new AtomicInteger(-1);
    private final AtomicInteger minRequestTime = new AtomicInteger(-1);
    // One bucket per minute, the extra one is the minute that is replaced next
    private final AtomicReferenceArray<Bucket> buckets = new AtomicReferenceArray<>(WINDOW_MINUTES + 1);
    private final Map<String, Map<Integer, LongAdder>> routeResponses = new ConcurrentHashMap<>();
    private final Deque<Failure> failures = new ConcurrentLinkedDeque<>();
    private final Clock clock;

    /** A response with a server error; neither path nor exception is kept, as both may contain data of the request. */
    public record Failure(Instant time, String method, String route, int status) {
    }

    /** Requests of the last five minutes, including the current one; maxRequestTime is -1 without requests. */
    public record Window(long requests, long errors, long avgRequestTime, int maxRequestTime) {
    }

    private static final class Bucket {
        private final long minute;
        private final LongAdder requests = new LongAdder();
        private final LongAdder errors = new LongAdder();
        private final LongAdder requestTime = new LongAdder();
        private final AtomicInteger maxRequestTime = new AtomicInteger(-1);

        private Bucket(long minute) {
            this.minute = minute;
        }
    }

    public Metrics() {
        this(Clock.systemUTC());
    }

    Metrics(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock can not be null");
    }

    public void addStatusCode(int responseCode) {
        responseCount
                .computeIfAbsent(responseCode, key -> new LongAdder())
                .increment();

        Bucket bucket = bucket();
        bucket.requests.increment();
        if (responseCode >= SERVER_ERROR) {
            bucket.errors.increment();
        }
    }

    public void update(final int requestTime) {
        Bucket bucket = bucket();
        bucket.requestTime.add(requestTime);
        bucket.maxRequestTime.updateAndGet(current -> Math.max(current, requestTime));

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

    /** Counts the response per route, e.g. "GET /user/{id}"; beyond 1000 routes the rest is counted together. */
    public void addRouteResponse(String route, int responseCode) {
        Objects.requireNonNull(route, "route can not be null");

        String key = routeResponses.containsKey(route) || routeResponses.size() < MAX_ROUTES ? route : OTHER_ROUTES;
        routeResponses
                .computeIfAbsent(key, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(responseCode, k -> new LongAdder())
                .increment();
    }

    public Map<String, Map<Integer, LongAdder>> getRouteResponses() {
        return routeResponses;
    }

    /** Keeps the last 50 server errors, the newest first. */
    public void addFailure(Failure failure) {
        failures.addFirst(Objects.requireNonNull(failure, "failure can not be null"));
        while (failures.size() > MAX_FAILURES) {
            failures.pollLast();
        }
    }

    public List<Failure> getFailures() {
        return List.copyOf(failures);
    }

    public Window getWindow() {
        long current = minute();
        long requests = 0;
        long errors = 0;
        long requestTime = 0;
        int max = -1;
        for (var i = 0; i < buckets.length(); i++) {
            Bucket bucket = buckets.get(i);
            if (bucket != null && current - bucket.minute < WINDOW_MINUTES) {
                requests += bucket.requests.sum();
                errors += bucket.errors.sum();
                requestTime += bucket.requestTime.sum();
                max = Math.max(max, bucket.maxRequestTime.get());
            }
        }

        return new Window(requests, errors, requests == 0 ? 0 : requestTime / requests, max);
    }

    private Bucket bucket() {
        long minute = minute();
        var index = (int) (minute % buckets.length());
        while (true) {
            Bucket bucket = buckets.get(index);
            if (bucket != null && bucket.minute == minute) {
                return bucket;
            }

            var next = new Bucket(minute);
            if (buckets.compareAndSet(index, bucket, next)) {
                return next;
            }
        }
    }

    private long minute() {
        return TimeUnit.MILLISECONDS.toMinutes(clock.millis());
    }

    public void reset() {
        for (var i = 0; i < buckets.length(); i++) {
            buckets.set(i, null);
        }
        responseCount.clear();
        routeResponses.clear();
        failures.clear();
        dataSend.set(0);
        totalRequestTime.set(0);
        totalRequests.set(0);
        maxRequestTime.set(-1);
        minRequestTime.set(-1);
    }
}