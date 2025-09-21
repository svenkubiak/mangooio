package io.mangoo.monitoring;

import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import io.mangoo.utils.Arguments;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class Telemetry {
    private static final Logger LOG = LogManager.getLogger(Telemetry.class);
    private final Map<String, Span> spans = new ConcurrentHashMap<>();
    private SdkTracerProvider traceProvider;
    private OpenTelemetry openTelemetry;

    @Inject
    public Telemetry(Config config) {
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
        }
    }

    public void shutdown() {
        if (traceProvider != null) {
            traceProvider.shutdown();
        }
    }

    public void begin(String process, String scope) {
        Arguments.requireNonBlank(process, Required.PROCESS);
        Arguments.requireNonBlank(scope, Required.SCOPE);

        Tracer tracer = openTelemetry.getTracer(scope);
        Span span = tracer.spanBuilder(process)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        spans.put(process, span);
    }

    public void begin(String parentProcess, String childProcess, String scope) {
        Arguments.requireNonBlank(parentProcess, Required.PROCESS);
        Arguments.requireNonBlank(childProcess, Required.PROCESS);
        Arguments.requireNonBlank(scope, Required.SCOPE);

        Span parentSpan = spans.get(parentProcess);
        if (parentSpan != null) {
            try (Scope ignored = parentSpan.makeCurrent()) {
                Tracer tracer = openTelemetry.getTracer(scope);
                Span child = tracer.spanBuilder(childProcess)
                        .setSpanKind(SpanKind.INTERNAL)
                        .startSpan();

                spans.put(childProcess, child);
            }
        }
    }

    public void end(String process) {
        Arguments.requireNonBlank(process, Required.PROCESS);

        Span span = spans.get(process);
        if (span != null) {
            try {
                span.end();
            } catch (Exception e) {
                LOG.error("Fail to end span", e);
            }
            spans.remove(process);
        }
    }

    public OpenTelemetry getOpenTelemetry() {
        return openTelemetry;
    }
}
