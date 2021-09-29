package io.mangoo.routing.listeners;

import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

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
    
    public MetricsListener() {
      //Empty constructor required for Google Guice
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
            metrics.update(processTime);
            metrics.addStatusCode(exchange.getStatusCode());
            
            long contentLength = exchange.getResponseContentLength();
            if (contentLength > 0) {
                metrics.incrementDataSend(contentLength);
            }
        }
        
        nextListener.proceed();
    }
}