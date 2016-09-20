package io.mangoo.routing.handlers;

import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.core.Application;
import io.mangoo.enums.CacheName;
import io.mangoo.providers.CacheProvider;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class LimitHandler implements HttpHandler {
    private Attachment requestAttachment;
    private Cache cache;
    
    @Inject
    public LimitHandler(CacheProvider cacheProvider) {
        Objects.requireNonNull(cacheProvider, "cacheProvider can not be null");
        this.cache = cacheProvider.getCache(CacheName.REQUEST);
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.requestAttachment = exchange.getAttachment(RequestUtils.ATTACHMENT_KEY);
        if (this.requestAttachment.hasLimit()) {
            String key = getCacheKey(exchange);
            if (this.cache.increment(key).get() > this.requestAttachment.getLimit()) {
                endRequest(exchange); 
            } else {
                nextHandler(exchange);
            }
        } else {
            nextHandler(exchange);
        }
    }
    
    /**
     * Creates a key for used for limit an request containing the
     * requested url and the source host
     * 
     * @param exchange The HttpServerExchange
     * @return The key url + host
     */
    private String getCacheKey(HttpServerExchange exchange) {
        String url = exchange.getRequestURL();
        String host = exchange.getSourceAddress().getHostString();
        
        return url + host;
    }

    /**
     * Ends the current request by senden an HTTP 429
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.TOO_MANY_REQUESTS);
        exchange.endExchange(); 
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        if (this.requestAttachment.hasAuthentication()) {
            HttpHandler httpHandler = RequestUtils.wrapSecurity(
                    Application.getInstance(LocaleHandler.class), this.requestAttachment.getUsername(), this.requestAttachment.getPassword());
            httpHandler.handleRequest(exchange);
        } else {
            Application.getInstance(LocaleHandler.class).handleRequest(exchange);    
        }
    }
}