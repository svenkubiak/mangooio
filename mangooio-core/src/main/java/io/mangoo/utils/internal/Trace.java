package io.mangoo.utils.internal;

import io.mangoo.constants.Const;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.utils.Argument;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;

public final class Trace {
    private static final Logger LOG = LogManager.getLogger(Trace.class);
    private static final Map<String, Span> SPANS = new ConcurrentHashMap<>();
    private static final Map<String, Scope> SCOPES = new ConcurrentHashMap<>();
    private static final Map<String, ScheduledFuture<?>> SCHEDULED_CLOSURES = new ConcurrentHashMap<>();
    private static final boolean ENABLED;
    private static final Duration AUTO_CLOSE_TIMEOUT = Duration.ofMinutes(2);
    private static final ScheduledExecutorService SCHEDULER;
    private static SdkTracerProvider tracerProvider;
    private static OpenTelemetry openTelemetry;

    static {
        Config config = Application.getInstance(Config.class);
        ENABLED = config.isOtlpEnable();
        if (ENABLED) {
            var resource = Resource.getDefault().merge(
                    Resource.create(
                            Attributes.of(
                                    AttributeKey.stringKey("service.name"), Const.FRAMEWORK,
                                    AttributeKey.stringKey("framework.name"), Const.FRAMEWORK,
                                    AttributeKey.stringKey("framework.version"), MangooUtils.getVersion(),
                                    AttributeKey.stringKey("framework.environment"), Application.getMode().name()
                            )));

            var otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
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

        SCHEDULER = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
    }

    private Trace() {}

    public static void shutdown() {
        if (tracerProvider != null) {
            try {
                tracerProvider.forceFlush().join(5, TimeUnit.SECONDS);
                tracerProvider.shutdown().join(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOG.error("Failed to shutdown tracer provider cleanly", e);
            }
        }
        SCHEDULER.shutdown();
        try {
            if (!SCHEDULER.awaitTermination(5, TimeUnit.SECONDS)) {
                SCHEDULER.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted during scheduler shutdown", e);
            SCHEDULER.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void start(String process) {
        if (!ENABLED) {return;}

        Argument.requireNonBlank(process, Required.PROCESS);

        if (openTelemetry != null) {
            var tracer = openTelemetry.getTracer(Const.FRAMEWORK);
            var span = tracer.spanBuilder(process)
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();

            var scope = span.makeCurrent();
            String key = getKey(process);
            SPANS.put(key, span);
            SCOPES.put(key, scope);

            ScheduledFuture<?> scheduledClose = SCHEDULER.schedule(() -> {
                if (SPANS.containsKey(key)) {
                    LOG.warn("Automatically closing span {} due to timeout", process);
                    end(process);
                }
            }, AUTO_CLOSE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            ScheduledFuture<?> previous = SCHEDULED_CLOSURES.put(key, scheduledClose);
            if (previous != null) {
                previous.cancel(false);
            }
        }
    }

    public static void startChild(String parentProcess, String childProcess) {
        if (!ENABLED) {return;}

        Argument.requireNonBlank(parentProcess, Required.PROCESS);
        Argument.requireNonBlank(childProcess, Required.PROCESS);

        if (openTelemetry != null) {
            var parentSpan = SPANS.get(getKey(parentProcess));
            if (parentSpan != null) {
                try (var ignored = parentSpan.makeCurrent()) {
                    var tracer = openTelemetry.getTracer(Const.FRAMEWORK);
                    var child = tracer.spanBuilder(childProcess)
                            .setSpanKind(SpanKind.INTERNAL)
                            .startSpan();
                    var scope = child.makeCurrent();

                    String key = getKey(childProcess);
                    SPANS.put(key, child);
                    SCOPES.put(key, scope);

                    ScheduledFuture<?> scheduledClose = SCHEDULER.schedule(() -> {
                        if (SPANS.containsKey(key)) {
                            LOG.warn("Automatically closing child span {} due to timeout", childProcess);
                            end(childProcess);
                        }
                    }, AUTO_CLOSE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

                    ScheduledFuture<?> previous = SCHEDULED_CLOSURES.put(key, scheduledClose);
                    if (previous != null) {
                        previous.cancel(false);
                    }
                }
            } else {
                LOG.warn("No parent span found for {}", parentProcess);
            }
        }
    }

    private static String getKey(String process) {
        Argument.requireNonBlank(process, Required.PROCESS);

        return process.replaceAll("[^a-z0-9]", "_")
                .toLowerCase(Locale.ENGLISH);
    }

    public static void end(String process) {
        if (!ENABLED) {return;}

        Argument.requireNonBlank(process, Required.PROCESS);

        String key = getKey(process);
        var span = SPANS.get(key);
        var scope = SCOPES.get(key);

        if (span != null) {
            try {
                span.end();
                if (scope != null) {
                    scope.close();
                }
            } catch (Exception e) {
                LOG.error("Failed to end span {}", process, e);
            } finally {
                SPANS.remove(key);
                SCOPES.remove(key);

                ScheduledFuture<?> scheduledClose = SCHEDULED_CLOSURES.remove(key);
                if (scheduledClose != null) {
                    scheduledClose.cancel(false);
                }
            }
        } else {
            LOG.warn("Tried to end span {} but none was found", process);
        }
    }
}