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
import java.util.concurrent.TimeUnit;

public final class Trace {
    private static final Logger LOG = LogManager.getLogger(Trace.class);
    private static final Map<String, Span> spans = new ConcurrentHashMap<>();
    private static final Map<String, Scope> scopes = new ConcurrentHashMap<>();
    private static final boolean enabled;
    private static SdkTracerProvider tracerProvider;
    private static OpenTelemetry openTelemetry;

    static {
        Config config = Application.getInstance(Config.class);
        enabled = config.isOtlpEnable();
        if (enabled) {
            Resource resource = Resource.getDefault().merge(
                    Resource.create(
                            Attributes.of(
                                    AttributeKey.stringKey("service.name"), Const.MANGOO_IO,
                                    AttributeKey.stringKey("service.version"), MangooUtils.getVersion(),
                                    AttributeKey.stringKey("framework.name"), Const.FRAMEWORK
                            )));

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

            LOG.info("OpenTelemetry tracing enabled with endpoint {}", config.getOtlpEndpoint());
        } else {
            LOG.info("OpenTelemetry tracing disabled");
        }
    }

    private Trace() {}

    public static void shutdown() {
        if (tracerProvider != null) {
            try {
                LOG.info("Forcing flush of spans before shutdown");
                tracerProvider.forceFlush().join(5, TimeUnit.SECONDS);
                tracerProvider.shutdown().join(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOG.error("Failed to shutdown tracer provider cleanly", e);
            }
        }
    }

    public static void start(String process, String scopeName) {
        if (!enabled) {return;}

        Arguments.requireNonBlank(process, Required.PROCESS);
        if (StringUtils.isBlank(scopeName)) {scopeName = Strings.EMPTY;}

        if (openTelemetry != null) {
            Tracer tracer = openTelemetry.getTracer(Const.FRAMEWORK);
            Span span = tracer.spanBuilder(process)
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();

            Scope scope = span.makeCurrent();
            String key = getKey(process);
            spans.put(key, span);
            scopes.put(key, scope);

            LOG.debug("Started span {} with key {}", process, key);
        }
    }

    public static void start(String process) {
        start(process, Strings.EMPTY);
    }

    public static void startChild(String parentProcess, String childProcess) {
        if (!enabled) {return;}

        Arguments.requireNonBlank(parentProcess, Required.PROCESS);
        Arguments.requireNonBlank(childProcess, Required.PROCESS);

        if (openTelemetry != null) {
            Span parentSpan = spans.get(getKey(parentProcess));
            if (parentSpan != null) {
                try (Scope ignored = parentSpan.makeCurrent()) {
                    Tracer tracer = openTelemetry.getTracer(Const.FRAMEWORK);
                    Span child = tracer.spanBuilder(childProcess)
                            .setSpanKind(SpanKind.INTERNAL)
                            .startSpan();
                    Scope scope = child.makeCurrent();

                    String key = getKey(childProcess);
                    spans.put(key, child);
                    scopes.put(key, scope);

                    LOG.debug("Started child span {} for parent {}", childProcess, parentProcess);
                }
            } else {
                LOG.warn("No parent span found for {}", parentProcess);
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

        String key = getKey(process);
        Span span = spans.get(key);
        Scope scope = scopes.get(key);
        if (span != null) {
            try {
                span.end();
                if (scope != null) {
                    scope.close();
                }
                LOG.debug("Ended span {}", process);
            } catch (Exception e) {
                LOG.error("Fail to end span {}", process, e);
            } finally {
                spans.remove(key);
                scopes.remove(key);
            }
        } else {
            LOG.warn("Tried to end span {} but none was found", process);
        }
    }
}
