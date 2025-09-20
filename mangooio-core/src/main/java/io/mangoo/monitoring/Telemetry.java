package io.mangoo.monitoring;

import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import io.mangoo.utils.Arguments;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class Telemetry {
    private final Map<String, Span> spans = new ConcurrentHashMap<>();
    private SdkTracerProvider traceProvider;
    private OpenTelemetry openTelemetry;
    private Tracer tracer;

    @Inject
    public Telemetry (Config config) {
        Objects.requireNonNull(config, Required.CONFIG);

        if (config.isOtlpEnable()) {
            OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
                    .setEndpoint(config.getOtlpEndpoint())
                    .build();

            this.traceProvider = SdkTracerProvider.builder()
                    .setResource(Resource.getDefault().merge(Resource.create(
                            Attributes.of(AttributeKey.stringKey("service.name"), config.getOtlpService())
                    )))
                    .addSpanProcessor(BatchSpanProcessor.builder(otlpGrpcSpanExporter).build())
                    .build();

            this.openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(this.traceProvider)
                    .build();

            this.tracer = openTelemetry.getTracer(config.getOtlpScopeName());
        }
    }

    public void shutdown() {
        if (traceProvider != null) {
            traceProvider.shutdown();
        }
    }

    public void begin(String process) {
        Arguments.requireNonBlank(process, Required.PROCESS);
        spans.put(process, tracer.spanBuilder(process).startSpan());
    }

    public void begin(String parentProcess, String childProcess) {
        Span parentSpan = spans.get(parentProcess);
        if (parentSpan != null) {
            Span child = tracer.spanBuilder(childProcess)
                    .setParent(Context.current().with(parentSpan))
                    .startSpan();

            spans.put(childProcess, child);
        }
    }

    public void end(String process) {
        Arguments.requireNonBlank(process, Required.PROCESS);

        Span span = spans.get(process);
        if (span != null) {
            span.end();
        }
        spans.remove(process);
    }

    public OpenTelemetry getOpenTelemetry() {
        return openTelemetry;
    }
}
