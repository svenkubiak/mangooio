package io.mangoo.routing.listeners;

import java.util.Objects;
import java.util.Optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.core.Application;
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

    @Inject
    public MetricsListener(Metrics metrics) {
        this.metrics = Objects.requireNonNull(metrics, "metrics can not be null");
    }

    @Override
    public void beforeCommit(HttpServerExchange exchange) {
        String uri = Optional.ofNullable(exchange.getRequestURI()).orElse("").replace("/", "").toLowerCase();
        if (!Application.getAdministrativeURLs().contains(uri)) {
            this.metrics.inc(exchange.getStatusCode());
        }
    }
}