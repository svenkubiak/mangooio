package io.mangoo.utils.internal;

import io.mangoo.constants.Const;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Trace {
    private static final Logger LOG = LogManager.getLogger(Trace.class);
    private static final Map<String, Span> spans = new ConcurrentHashMap<>();
    private static SdkTracerProvider tracerProvider;
    private static OpenTelemetry openTelemetry;
    private static boolean enabled;

    private Trace() {
        Config config = Application.getInstance(Config.class);
        enabled = config.isOtlpEnable();
        if (enabled) {
            Resource resource = Resource.getDefault().merge(
                    Resource.create(
                            Attributes.of(
                                    AttributeKey.stringKey("service.name"), Const.MANGOO_IO,
                                    AttributeKey.stringKey("service.version"), MangooUtils.getVersion(),
                                    AttributeKey.stringKey("framework.name"), Const.FRAMEWORK)));

            OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
                    .setEndpoint(config.getOtlpEndpoint())
                    .build();

            tracerProvider = SdkTracerProvider.builder()
                    .setResource(resource)
                    .addSpanProcessor(BatchSpanProcessor.builder(otlpGrpcSpanExporter).build())
                    .build();

            openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(tracerProvider)
                    .build();
        }
    }

    public static void shutdown() {
        if (tracerProvider != null) {
            tracerProvider.shutdown();
        }
    }

    public static void start(String process, String scope) {
        if (!enabled) {return;}

        Arguments.requireNonBlank(process, Required.PROCESS);
        if (StringUtils.isBlank(scope)) {scope = Strings.EMPTY;}

        if (openTelemetry != null) {
            Tracer tracer = openTelemetry.getTracer(scope);
            Span span = tracer.spanBuilder(process)
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();

            spans.put(getKey(process), span);
        }
    }

    public static void start(String process) {
        if (!enabled) {return;}

        Arguments.requireNonBlank(process, Required.PROCESS);
        start(process, Strings.EMPTY);
    }

    public static void startChild(String parentProcess, String childProcess) {
        if (!enabled) {return;}

        Arguments.requireNonBlank(parentProcess, Required.PROCESS);
        Arguments.requireNonBlank(childProcess, Required.PROCESS);

        if (openTelemetry != null) {
            Span parentSpan = spans.get(parentProcess);
            if (parentSpan != null) {
                try (Scope ignored = parentSpan.makeCurrent()) {
                    Tracer tracer = openTelemetry.getTracer(Strings.EMPTY);
                    Span child = tracer.spanBuilder(childProcess)
                            .setSpanKind(SpanKind.INTERNAL)
                            .startSpan();

                    spans.put(getKey(childProcess), child);
                }
            }
        }
    }

    private static String getKey(String process) {
        Arguments.requireNonBlank(process, Required.PROCESS);

        return process
                .replaceAll("[^a-z0-9]", "_")
                .toLowerCase(Locale.ENGLISH);
    }

    public static void end(String process) {
        if (!enabled) {return;}

        Arguments.requireNonBlank(process, Required.PROCESS);

        Span span = spans.get(process);
        if (span != null) {
            try {
                span.end();
            } catch (Exception e) {
                LOG.error("Fail to end span", e);
            }
            spans.remove(getKey(process));
        }
    }
}
