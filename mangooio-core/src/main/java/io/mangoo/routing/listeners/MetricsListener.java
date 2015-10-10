package io.mangoo.routing.listeners;

import java.util.Arrays;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.models.Metrics;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ResponseCommitListener;

@Singleton
public class MetricsListener implements ResponseCommitListener {
    private Metrics metrics;
    private List<String> blacklist = Arrays.asList("/@cache", "/@metrics", "/@config", "/@routes", "/@health");

    @Inject
    public MetricsListener(Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void beforeCommit(HttpServerExchange exchange) {
        if (!blacklist.contains(exchange.getRequestURI())) {
            this.metrics.inc(exchange.getStatusCode());
        }
    }
}