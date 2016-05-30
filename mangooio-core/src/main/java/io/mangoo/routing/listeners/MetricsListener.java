package io.mangoo.routing.listeners;

import java.util.Optional;

import io.mangoo.core.Application;
import io.mangoo.models.Metrics;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;

/**
 * 
 * @author svenkubiak
 *
 */
public class MetricsListener implements ExchangeCompletionListener {
    private long start;

    public MetricsListener(long start) {
        this.start = start;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        Metrics metrics = Application.getInstance(Metrics.class);
        metrics.update((int) (System.currentTimeMillis() - this.start));
        
        String uri = Optional.ofNullable(exchange.getRequestURI()).orElse("").replace("/", "").toLowerCase();
        if (!uri.contains("@admin")) {
            metrics.inc(exchange.getStatusCode());
        }
        
        nextListener.proceed();
    }
}