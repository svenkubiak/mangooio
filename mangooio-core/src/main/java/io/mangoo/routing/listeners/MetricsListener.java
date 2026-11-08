package io.mangoo.routing.listeners;

import io.mangoo.core.Application;
import io.mangoo.models.Metrics;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public class MetricsListener implements ExchangeCompletionListener {
    private static final int SERVER_ERROR = 500;
    private long start;
    
    public MetricsListener() {
      // Empty constructor for Google Guice
    }

    public MetricsListener(long start) {
        this.start = start;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        String path = Optional.ofNullable(exchange.getRequestURI()).orElse("");
        String uri = path.toLowerCase(Locale.ENGLISH);
        
        if (StringUtils.isNotBlank(uri) && !uri.contains("@admin")) {
            int processTime = (int) (System.currentTimeMillis() - this.start);
            
            var metrics = Application.getInstance(Metrics.class);
            int status = exchange.getStatusCode();
            var method = exchange.getRequestMethod().toString();
            metrics.update(processTime);
            metrics.addStatusCode(status);

            // A matched route keeps its template, so ids in the path are not stored. An unmatched request has no template, so the path is its name.
            PathTemplateMatch match = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String route = match == null ? path : match.getMatchedTemplate();
            metrics.addRouteResponse(method + " " + route, status);

            if (status >= SERVER_ERROR) {
                metrics.addFailure(new Metrics.Failure(Instant.now(), method, route, status));
            }
            
            long contentLength = exchange.getResponseContentLength();
            if (contentLength > 0) {
                metrics.incrementDataSend(contentLength);
            }
        }
        
        nextListener.proceed();
    }
}