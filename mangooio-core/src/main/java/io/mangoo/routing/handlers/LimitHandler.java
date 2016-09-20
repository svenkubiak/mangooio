package io.mangoo.routing.handlers;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.Inject;
import com.tc.text.StringUtils;

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
            String url = exchange.getRequestURL();
            InetSocketAddress inetSocketAddress = exchange.getSourceAddress();
            if (StringUtils.isNotBlank(url) && inetSocketAddress != null) {
                String hostString = inetSocketAddress.getHostString();
                String key = hostString.trim().toLowerCase() + url.trim().toLowerCase();
                
                AtomicInteger counter = this.cache.get(key) ;
                if (counter == null) {
                    counter = new AtomicInteger();
                }
                
                if (this.requestAttachment.getLimit() >= counter.get()) {
                    counter.incrementAndGet();
                    this.cache.put(key, counter);   
                } else {
                    endRequest(exchange); 
                }
            } else {
                endRequest(exchange);
            }
        } else {
            nextHandler(exchange);
        }
    }
    
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
            HttpHandler httpHandler = RequestUtils.wrapSecurity(Application.getInstance(LocaleHandler.class), this.requestAttachment.getUsername(), this.requestAttachment.getPassword());
            httpHandler.handleRequest(exchange);
        } else {
            Application.getInstance(LocaleHandler.class).handleRequest(exchange);    
        }
    }
}