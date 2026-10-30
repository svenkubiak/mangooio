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
import io.opentelemetry.context.Context;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class Trace {
    private static final Logger LOG = LogManager.getLogger(Trace.class);
    private static final AttachmentKey<TraceState> TRACE_STATE = AttachmentKey.create(TraceState.class);
    private static final AtomicLong STARTED_SPANS = new AtomicLong();
    private static final AtomicLong ENDED_SPANS = new AtomicLong();
    private static volatile boolean initialized;
    private static boolean enabled;
    private static SdkTracerProvider tracerProvider;
    private static SdkMeterProvider meterProvider;
    private static RequestMetrics requestMetrics;
    private static OpenTelemetry openTelemetry;

    private Trace() {}

    // DatastoreImpl is constructed while Guice is still creating the injector, so Config has to be passed in
    public static OpenTelemetry getOpenTelemetry(Config config) {
        init(config);
        return openTelemetry;
    }

    private static void ensureReady() {
        if (!initialized) {
            init(Application.getInstance(Config.class));
        }
    }

    private static boolean ready() {
        ensureReady();
        return enabled && openTelemetry != null;
    }

    private static void init(Config config) {
        if (initialized) {
            return;
        }
        synchronized (Trace.class) {
            if (initialized) {
                return;
            }
            Objects.requireNonNull(config, Required.CONFIG);
            enabled = config.isOtlpEnable();
            if (enabled) {
                var resource = resource(config.getApplicationName(), Application.getInstanceId(), MangooUtils.getVersion(), String.valueOf(Application.getMode()));

                var otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
                        .setEndpoint(config.getOtlpEndpoint())
                        .build();

                tracerProvider = SdkTracerProvider.builder()
                        .setResource(resource)
                        .addSpanProcessor(BatchSpanProcessor.builder(otlpGrpcSpanExporter).build())
                        .build();

                var otlpGrpcMetricExporter = OtlpGrpcMetricExporter.builder()
                        .setEndpoint(config.getOtlpEndpoint())
                        .build();

                meterProvider = SdkMeterProvider.builder()
                        .setResource(resource)
                        .registerMetricReader(PeriodicMetricReader.builder(otlpGrpcMetricExporter).build())
                        .build();

                openTelemetry = OpenTelemetrySdk.builder()
                        .setTracerProvider(tracerProvider)
                        .setMeterProvider(meterProvider)
                        .build();
                requestMetrics = new RequestMetrics(openTelemetry.getMeter(Const.FRAMEWORK));

                LOG.info("OpenTelemetry tracing and metrics enabled with endpoint {}", config.getOtlpEndpoint());
            } else {
                LOG.info("OpenTelemetry tracing and metrics disabled");
            }
            initialized = true;
        }
    }

    // service.name identifies the application, so several applications built with mangoo I/O stay apart in one backend
    static Resource resource(String applicationName, String instanceId, String version, String mode) {
        return Resource.getDefault().merge(
                Resource.create(
                        Attributes.of(
                                AttributeKey.stringKey("service.name"), applicationName,
                                AttributeKey.stringKey("service.instance.id"), instanceId,
                                AttributeKey.stringKey("framework.name"), Const.FRAMEWORK,
                                AttributeKey.stringKey("framework.version"), version,
                                AttributeKey.stringKey("framework.environment"), mode
                        )));
    }

    public static void shutdown() {
        ensureReady();
        if (tracerProvider != null) {
            try {
                tracerProvider.forceFlush().join(5, TimeUnit.SECONDS);
                tracerProvider.shutdown().join(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOG.error("Failed to shutdown tracer provider cleanly", e);
            }
        }
        if (meterProvider != null) {
            try {
                meterProvider.forceFlush().join(5, TimeUnit.SECONDS);
                meterProvider.shutdown().join(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOG.error("Failed to shutdown meter provider cleanly", e);
            }
        }
    }

    /** Records the duration of a completed request; does nothing unless otlp.enable is true. */
    public static void recordRequest(HttpServerExchange exchange, long nanos) {
        if (!ready() || requestMetrics == null) {return;}

        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);
        requestMetrics.record(exchange.getRequestMethod().toString(), exchange.getStatusCode(), nanos,
                exchange.getResponseHeaders().getFirst(Headers.CONTENT_TYPE));
    }

    // A completion listener ends every span of the exchange, even if the handler chain aborts before end() is reached
    public static void start(HttpServerExchange exchange, String process) {
        if (!ready()) {return;}

        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);
        Argument.requireNonBlank(process, Required.PROCESS);

        if (exchange.getAttachment(TRACE_STATE) != null) {
            LOG.warn("Tried to start span {} but this exchange is already traced", process);
            return;
        }

        var traceState = new TraceState();
        exchange.putAttachment(TRACE_STATE, traceState);
        exchange.addExchangeCompleteListener((completedExchange, nextListener) -> {
            try {
                endAll(completedExchange);
            } finally {
                nextListener.proceed();
            }
        });

        traceState.push(createSpan(process, Context.root()));
    }

    // The parent context is passed explicitly, so the child span is correct regardless of the current thread
    public static void startChild(HttpServerExchange exchange, String childProcess) {
        if (!ready()) {return;}

        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);
        Argument.requireNonBlank(childProcess, Required.PROCESS);

        var traceState = exchange.getAttachment(TRACE_STATE);
        var parent = traceState != null ? traceState.peek() : null;

        if (parent == null) {
            LOG.warn("No parent span found for {}", childProcess);
            return;
        }

        traceState.push(createSpan(childProcess, Context.root().with(parent)));
    }

    public static void end(HttpServerExchange exchange) {
        if (!ready()) {return;}

        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        var traceState = exchange.getAttachment(TRACE_STATE);
        var span = traceState != null ? traceState.pop() : null;

        if (span != null) {
            endSpan(span);
        } else {
            LOG.debug("Tried to end a span but none was open for this exchange");
        }
    }

    private static void endAll(HttpServerExchange exchange) {
        var traceState = exchange.removeAttachment(TRACE_STATE);
        if (traceState == null) {
            return;
        }

        Span span;
        while ((span = traceState.pop()) != null) {
            endSpan(span);
        }
    }

    /** Number of spans started since startup; equals getEndedSpans() once all exchanges are complete. */
    public static long getStartedSpans() {
        return STARTED_SPANS.get();
    }

    public static long getEndedSpans() {
        return ENDED_SPANS.get();
    }

    private static Span createSpan(String process, Context parent) {
        var span = openTelemetry.getTracer(Const.FRAMEWORK)
                .spanBuilder(process)
                .setSpanKind(SpanKind.INTERNAL)
                .setParent(parent)
                .startSpan();
        STARTED_SPANS.incrementAndGet();

        return span;
    }

    private static void endSpan(Span span) {
        try {
            span.end();
            ENDED_SPANS.incrementAndGet();
        } catch (Exception e) {
            LOG.error("Failed to end span", e);
        }
    }

    // Synchronized because the exchange completion listener may run on a different thread than the handler chain
    private static final class TraceState {
        private final Deque<Span> spans = new ArrayDeque<>();

        private synchronized void push(Span span) {
            spans.push(span);
        }

        private synchronized Span peek() {
            return spans.peek();
        }

        private synchronized Span pop() {
            return spans.poll();
        }
    }
}
