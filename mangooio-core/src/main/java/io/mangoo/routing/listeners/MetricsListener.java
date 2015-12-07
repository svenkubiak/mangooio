package io.mangoo.routing.listeners;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.models.Metrics;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ResponseCommitListener;

/**
 * Listener that is invoked for counting metrics of HTTP response codes
 *
 * @author Kubiak
 *
 */
@Singleton
public class MetricsListener implements ResponseCommitListener {
    private final Metrics metrics;
    private final List<String> blacklist = Arrays.asList("@cache", "@metrics", "@config", "@routes", "@health", "@scheduler");

    @Inject
    public MetricsListener(Metrics metrics) {
        this.metrics = Objects.requireNonNull(metrics, "metrics can not be null");
    }

    @Override
    public void beforeCommit(HttpServerExchange exchange) {
        String uri = Optional.ofNullable(exchange.getRequestURI()).orElse("").replace("/", "").toLowerCase();
        if (!blacklist.contains(uri)) {
            this.metrics.inc(exchange.getStatusCode());
        }
    }
}