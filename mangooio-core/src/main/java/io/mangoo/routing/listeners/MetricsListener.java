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
    private static final String NO_ROUTE = "(no route)";
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
        String uri = Optional.ofNullable(exchange.getRequestURI())
                .orElse("")
                .toLowerCase(Locale.ENGLISH);
        
        if (StringUtils.isNotBlank(uri) && !uri.contains("@admin")) {
            int processTime = (int) (System.currentTimeMillis() - this.start);
            
            var metrics = Application.getInstance(Metrics.class);
            int status = exchange.getStatusCode();
            String method = exchange.getRequestMethod().toString();
            metrics.update(processTime);
            metrics.addStatusCode(status);

            // The route template instead of the path, so the number of entries does not grow with the ids in the paths
            PathTemplateMatch match = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String route = match == null ? NO_ROUTE : match.getMatchedTemplate();
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