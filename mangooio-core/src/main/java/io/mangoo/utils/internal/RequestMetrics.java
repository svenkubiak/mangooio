package io.mangoo.utils.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import org.apache.commons.lang3.Strings;

import java.util.List;
import java.util.Objects;

final class RequestMetrics {
    private static final AttributeKey<String> METHOD = AttributeKey.stringKey("http.request.method");
    private static final AttributeKey<Long> STATUS = AttributeKey.longKey("http.response.status_code");
    private static final double NANOS_PER_SECOND = 1_000_000_000d;
    private final DoubleHistogram duration;

    RequestMetrics(Meter meter) {
        Objects.requireNonNull(meter, "meter can not be null");

        this.duration = meter.histogramBuilder("http.server.request.duration")
                .setUnit("s")
                .setDescription("Duration of HTTP server requests")
                .setExplicitBucketBoundariesAdvice(List.of(0.005, 0.01, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5, 0.75, 1.0, 2.5, 5.0, 7.5, 10.0))
                .build();
    }

    // A server sent event stream lasts as long as the connection and would distort the request durations
    void record(String method, int status, long nanos, String contentType) {
        if (Strings.CI.startsWith(contentType, "text/event-stream")) {
            return;
        }

        duration.record(nanos / NANOS_PER_SECOND, Attributes.of(METHOD, method, STATUS, (long) status));
    }
}
