package io.mangoo.routing.handlers;

import java.net.InetSocketAddress;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.core.Application;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.helpers.RequestHelper;
import io.mangoo.providers.CacheProvider;
import io.mangoo.routing.Attachment;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class LimitHandler implements HttpHandler {
    private Attachment attachment;
    private Cache cache;
    private final RequestHelper requestHelper;
    
    @Inject
    public LimitHandler(CacheProvider cacheProvider, RequestHelper requestHelper) {
        Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
        this.cache = cacheProvider.getCache(CacheName.REQUEST);
        this.requestHelper = Objects.requireNonNull(requestHelper, Required.REQUEST_HELPER.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.attachment = exchange.getAttachment(RequestHelper.ATTACHMENT_KEY);
        if (this.attachment.hasLimit()) {
            String key = getCacheKey(exchange);
            if (StringUtils.isNotBlank(key)) {
                if (this.cache.increment(key).get() > this.attachment.getLimit()) {
                    endRequest(exchange); 
                } else {
                    nextHandler(exchange);
                } 
            } else {
                endRequest(exchange);                 
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
        String host = null;

        HeaderMap headerMap = exchange.getRequestHeaders();
        if (headerMap != null) {
            HeaderValues headerValues = headerMap.get(Header.X_FORWARDED_FOR.toHttpString());
            if (headerValues != null) {
                host = headerValues.element();
            }
        }
        
        if (StringUtils.isBlank(host)) {
            InetSocketAddress inetSocketAddress = exchange.getSourceAddress();
            if (inetSocketAddress != null) {
                host = inetSocketAddress.getHostString();
            }
        }
        
        if (StringUtils.isNotBlank(host)) {
            host = host.toLowerCase();
        }
        
        String url = exchange.getRequestURL();
        if (StringUtils.isNotBlank(url)) {
            url = url.toLowerCase();
        }
        
        return url + host;
    }

    /**
     * Ends the current request by sending a HTTP 429 status code
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
        if (this.attachment.hasAuthentication()) {
            HttpHandler httpHandler = this.requestHelper.wrapSecurity(
                    Application.getInstance(LocaleHandler.class),
                    this.attachment.getUsername(),
                    this.attachment.getPassword());
            
            httpHandler.handleRequest(exchange);
        } else {
            Application.getInstance(LocaleHandler.class).handleRequest(exchange);    
        }
    }
}