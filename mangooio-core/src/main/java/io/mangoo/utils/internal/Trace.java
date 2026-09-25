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
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class Trace {
    private static final Logger LOG = LogManager.getLogger(Trace.class);
    private static final AttachmentKey<TraceState> TRACE_STATE = AttachmentKey.create(TraceState.class);
    private static final boolean ENABLED;
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
    }

    /**
     * Starts the root span for the given exchange. The span is attached to the exchange and is
     * therefore isolated from any other request that is processed in parallel. An exchange
     * completion listener ensures that every span of this exchange is ended, even if the
     * handler chain is aborted before {@link #end(HttpServerExchange)} is reached.
     *
     * @param exchange The Undertow HttpServerExchange
     * @param process The name of the span
     */
    public static void start(HttpServerExchange exchange, String process) {
        if (!ENABLED || openTelemetry == null) {return;}

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

    /**
     * Starts a child span of the currently innermost span of the given exchange. The parent
     * context is propagated explicitly, so the child span is correct regardless of the thread
     * the request is currently processed on.
     *
     * @param exchange The Undertow HttpServerExchange
     * @param childProcess The name of the child span
     */
    public static void startChild(HttpServerExchange exchange, String childProcess) {
        if (!ENABLED || openTelemetry == null) {return;}

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

    /**
     * Ends the innermost span that is currently open for the given exchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    public static void end(HttpServerExchange exchange) {
        if (!ENABLED) {return;}

        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        var traceState = exchange.getAttachment(TRACE_STATE);
        var span = traceState != null ? traceState.pop() : null;

        if (span != null) {
            endSpan(span);
        } else {
            LOG.debug("Tried to end a span but none was open for this exchange");
        }
    }

    /**
     * Ends all spans that are still open for the given exchange and detaches the trace state
     *
     * @param exchange The Undertow HttpServerExchange
     */
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

    private static Span createSpan(String process, Context parent) {
        return openTelemetry.getTracer(Const.FRAMEWORK)
                .spanBuilder(process)
                .setSpanKind(SpanKind.INTERNAL)
                .setParent(parent)
                .startSpan();
    }

    private static void endSpan(Span span) {
        try {
            span.end();
        } catch (Exception e) {
            LOG.error("Failed to end span", e);
        }
    }

    /**
     * Holds the span stack of a single exchange. Access is synchronized because the exchange
     * completion listener may run on a different thread than the handler chain.
     */
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
