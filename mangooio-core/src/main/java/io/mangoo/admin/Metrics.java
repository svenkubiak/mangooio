package io.mangoo.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import com.google.inject.Singleton;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Metrics {
    private final Map<Integer, LongAdder> metrics = new HashMap<Integer, LongAdder>();

    public void inc(int responseCode) {
        this.metrics.computeIfAbsent(responseCode, (t) -> new LongAdder()).increment();
    }

    public Map<Integer, LongAdder> getMetrics() {
        return this.metrics;
    }
}