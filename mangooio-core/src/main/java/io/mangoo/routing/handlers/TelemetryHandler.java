package io.mangoo.routing.handlers;

import io.mangoo.utils.internal.Trace;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Objects;

/** Measures every request for the OpenTelemetry metrics; only part of the handler chain when otlp.enable is true. */
public class TelemetryHandler implements HttpHandler {
    private final HttpHandler next;

    public TelemetryHandler(HttpHandler next) {
        this.next = Objects.requireNonNull(next, "next can not be null");
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        long start = System.nanoTime();
        if (!exchange.isComplete()) {
            exchange.addExchangeCompleteListener((completed, nextListener) -> {
                try {
                    Trace.recordRequest(completed, System.nanoTime() - start);
                } finally {
                    nextListener.proceed();
                }
            });
        }

        next.handleRequest(exchange);
    }
}
