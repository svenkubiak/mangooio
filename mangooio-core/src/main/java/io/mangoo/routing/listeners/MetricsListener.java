package io.mangoo.routing.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.admin.Metrics;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;

@Singleton
public class MetricsListener implements ExchangeCompletionListener {
    private Metrics metrics;

    @Inject
    public MetricsListener(Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        this.metrics.inc(exchange.getResponseCode());
    }
}