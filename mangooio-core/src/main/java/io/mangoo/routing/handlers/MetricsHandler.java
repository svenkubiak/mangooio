package io.mangoo.routing.handlers;

import io.mangoo.routing.listeners.MetricsListener;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * 
 * @author svenkubiak
 *
 */
public class MetricsHandler implements HttpHandler {
    public static final HandlerWrapper HANDLER_WRAPPER = MetricsHandler::new;
    private final HttpHandler nextHandler;

    public MetricsHandler(HttpHandler next) {
        this.nextHandler = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (!exchange.isComplete()) {
            exchange.addExchangeCompleteListener(new MetricsListener(System.currentTimeMillis()));
        }
        this.nextHandler.handleRequest(exchange);
    }
}