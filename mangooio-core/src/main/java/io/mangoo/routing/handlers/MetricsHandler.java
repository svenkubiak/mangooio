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
    private final HttpHandler next;
    
    public static final HandlerWrapper WRAPPER = new HandlerWrapper() {
        @Override
        public HttpHandler wrap(HttpHandler handler) {
            return new MetricsHandler(handler);
        }
    };

    public MetricsHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (!exchange.isComplete()) {
            exchange.addExchangeCompleteListener(new MetricsListener(System.currentTimeMillis()));
        }
        this.next.handleRequest(exchange);
    }
}