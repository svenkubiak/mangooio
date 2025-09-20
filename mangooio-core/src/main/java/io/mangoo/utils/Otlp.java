package io.mangoo.utils;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Otlp {
    private static final Map<String, Span> spans = new ConcurrentHashMap<>();
    private static final OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://10.0.0.2:4317")
            .build();

    private static final SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .setResource(Resource.getDefault().merge(Resource.create(
                    Attributes.of(AttributeKey.stringKey("service.name"), "household")
            )))
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .build();

    private static final OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();

    private static final Tracer tracer = openTelemetry.getTracer("household");

    public static OpenTelemetry getOpenTelemetry() {
        return openTelemetry;
    }

    // Start a span for the given process name and store it
    public static void begin(String process) {
        Span span = tracer.spanBuilder(process).startSpan();
        spans.put(process, span);
    }

    // End the span associated with the process and remove it
    public static void end(String process) {
        Span span = spans.get(process);
        if (span != null) {
            span.end();
        }
        spans.remove(process);
    }

    // Shutdown hook to flush spans before JVM exit
    public static void shutdown() {
        tracerProvider.shutdown();
    }
}
