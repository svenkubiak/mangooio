package io.mangoo.routing.handlers;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.Required;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;

/**
 * 
 * @author svenkubiak
 *
 */
public class CorsHandler implements HttpHandler {
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    private Config config;
    
    @Inject
    public CorsHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (this.config.isCorsEnable() && config.getCorsUrlPattern().matcher(exchange.getRequestURL()).matches()) {
            applyHeader(exchange);
        }
        nextHandler(exchange);
    }
    
    private void applyHeader(HttpServerExchange exchange) {
        String origin = getOrigin(exchange);
        if (StringUtils.isNotBlank(origin) && this.config.getCorsAllowOrigin().matcher(origin).matches()) {
            if (doesNotHaveHeader(ACCESS_CONTROL_ALLOW_ORIGIN, exchange)) {
                addHeader(exchange, ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            }
            
            if (doesNotHaveHeader(ACCESS_CONTROL_ALLOW_HEADERS, exchange)) {
                addHeader(exchange, ACCESS_CONTROL_ALLOW_HEADERS, this.config.getCorsHeadersAllowHeaders());
            }
            
            if (doesNotHaveHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, exchange)) {
                addHeader(exchange, ACCESS_CONTROL_ALLOW_CREDENTIALS, this.config.getCorsHeadersAllowCredentials());
            }
            
            if (doesNotHaveHeader(ACCESS_CONTROL_ALLOW_METHODS, exchange)) {
                addHeader(exchange, ACCESS_CONTROL_ALLOW_METHODS, this.config.getCorsHeadersAllowMethods());
            }
            
            if (doesNotHaveHeader(ACCESS_CONTROL_EXPOSE_HEADERS, exchange)) {
                addHeader(exchange, ACCESS_CONTROL_EXPOSE_HEADERS, this.config.getCorsHeadersExposeHeaders());
            }
            
            if (doesNotHaveHeader(ACCESS_CONTROL_MAX_AGE, exchange)) {
                addHeader(exchange, ACCESS_CONTROL_MAX_AGE, this.config.getCorsHeadersMaxAge());
            }
        }
    }

    private String getOrigin(HttpServerExchange exchange) {
        HeaderValues headers = exchange.getRequestHeaders().get("Origin");
        return headers == null ? null : headers.peekFirst();
    }
    
    private boolean doesNotHaveHeader(String name, HttpServerExchange exchange) {
        return exchange.getResponseHeaders().get(name) == null;
    }
    
    private void addHeader(HttpServerExchange exchange, String name, String value) {
        exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(ResponseHandler.class).handleRequest(exchange);
    }
}