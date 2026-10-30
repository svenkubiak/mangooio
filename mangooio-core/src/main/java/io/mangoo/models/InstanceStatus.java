package io.mangoo.models;

import java.time.Instant;

/**
 * State of one instance as reported by its heartbeat; requests, errors and avgRequestTime cover the last five minutes.
 */
public record InstanceStatus(
        String id,
        String version,
        Instant started,
        Instant seen,
        long heapUsed,
        long heapMax,
        int connections,
        long requests,
        long errors,
        long avgRequestTime,
        long failedEvents) {
}
