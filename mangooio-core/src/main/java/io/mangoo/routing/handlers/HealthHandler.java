package io.mangoo.routing.handlers;

import io.mangoo.constants.Default;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Answers the liveness and readiness probes before metrics, cookies and filters; every other request is passed on. */
public class HealthHandler implements HttpHandler {
    private final HttpHandler next;
    private final BooleanSupplier ready;

    public HealthHandler(HttpHandler next, BooleanSupplier ready) {
        this.next = Objects.requireNonNull(next, "next can not be null");
        this.ready = Objects.requireNonNull(ready, "ready can not be null");
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        var path = exchange.getRequestPath();
        var method = exchange.getRequestMethod();
        boolean probe = Default.HEALTH_LIVE.equals(path) || Default.HEALTH_READY.equals(path);

        if (probe && (Methods.GET.equals(method) || Methods.HEAD.equals(method))) {
            boolean healthy = Default.HEALTH_LIVE.equals(path) || ready.getAsBoolean();
            exchange.setStatusCode(healthy ? StatusCodes.OK : StatusCodes.SERVICE_UNAVAILABLE);
            exchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "no-store");
            exchange.endExchange();
        } else {
            next.handleRequest(exchange);
        }
    }
}
