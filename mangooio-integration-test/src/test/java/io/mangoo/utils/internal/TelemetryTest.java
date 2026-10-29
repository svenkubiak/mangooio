package io.mangoo.utils.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TelemetryTest {

    private static final class CollectingReader implements MetricReader {
        private CollectionRegistration registration = CollectionRegistration.noop();

        @Override
        public void register(CollectionRegistration registration) {
            this.registration = registration;
        }

        Collection<MetricData> collect() {
            return registration.collectAllMetrics();
        }

        @Override
        public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
            return AggregationTemporality.CUMULATIVE;
        }

        @Override
        public CompletableResultCode forceFlush() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    }

    @Test
    void testRequestDurationIsRecordedInSecondsWithMethodAndStatus() {
        //given
        var reader = new CollectingReader();
        try (var provider = SdkMeterProvider.builder().registerMetricReader(reader).build()) {
            var metrics = new RequestMetrics(provider.get("test"));

            //when
            metrics.record("GET", 200, 120_000_000L, "text/html");
            metrics.record("GET", 200, 30_000_000L, "text/html");
            metrics.record("POST", 500, 2_000_000_000L, null);

            //then
            MetricData metric = reader.collect().stream()
                    .filter(data -> data.getName().equals("http.server.request.duration"))
                    .findFirst()
                    .orElseThrow();
            assertThat(metric.getUnit(), equalTo("s"));

            List<HistogramPointData> points = List.copyOf(metric.getHistogramData().getPoints());
            assertThat(points, hasSize(2));
            HistogramPointData get = points.stream()
                    .filter(point -> "GET".equals(point.getAttributes().get(AttributeKey.stringKey("http.request.method"))))
                    .findFirst()
                    .orElseThrow();
            assertThat(get.getCount(), equalTo(2L));
            assertThat(get.getSum(), closeTo(0.15, 0.0001));
            assertThat(get.getAttributes().get(AttributeKey.longKey("http.response.status_code")), equalTo(200L));
            assertThat(get.getBoundaries(), hasItems(0.005, 0.1, 10.0));
        }
    }

    @Test
    void testServerSentEventStreamsAreNotRecorded() {
        //given
        var reader = new CollectingReader();
        try (var provider = SdkMeterProvider.builder().registerMetricReader(reader).build()) {
            var metrics = new RequestMetrics(provider.get("test"));

            //when
            metrics.record("GET", 200, 60_000_000_000L, "text/event-stream; charset=UTF-8");

            //then
            assertThat(reader.collect(), empty());
        }
    }

    @Test
    void testServiceNameIsTheApplicationName() {
        //when
        var resource = Trace.resource("shop", "web-1-abc123", "11.0.0", "PROD");

        //then
        assertThat(resource.getAttribute(AttributeKey.stringKey("service.name")), equalTo("shop"));
        assertThat(resource.getAttribute(AttributeKey.stringKey("service.instance.id")), equalTo("web-1-abc123"));
        assertThat(resource.getAttribute(AttributeKey.stringKey("framework.name")), equalTo("io.mangoo"));
        assertThat(resource.getAttribute(AttributeKey.stringKey("framework.version")), equalTo("11.0.0"));
    }
}
