package io.mangoo.routing.handlers;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.core.Application;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.enums.Template;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.RequestUtils;
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
    
    @Inject
    public LimitHandler(CacheProvider cacheProvider) {
        Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
        this.cache = cacheProvider.getCache(CacheName.REQUEST);
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        if (attachment.hasLimit()) {
            String key = getCacheKey(exchange);
            if (cache.getAndIncrementCounter(key).get() > attachment.getLimit()) {
                endRequest(exchange); 
            } else {
                nextHandler(exchange);
            } 
        } else {
            nextHandler(exchange);
        }
    }

    /**
     * Creates a key used for limiting a request containing the requested URL and the source host
     * 
     * @param exchange The HttpServerExchange
     * @return The key (URL + host)
     */
    private String getCacheKey(HttpServerExchange exchange) {
        String host = "";

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
            host = host.toLowerCase(Locale.ENGLISH);
        }
        
        String url = exchange.getRequestURL();
        if (StringUtils.isNotBlank(url)) {
            url = url.toLowerCase(Locale.ENGLISH);
        }
        
        return url + host;
    }

    /**
     * Ends the current request by sending a HTTP 429 status code
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.TOO_MANY_REQUESTS);
        exchange.getResponseSender().send(Template.DEFAULT.tooManyRequests());
        exchange.endExchange(); 
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        if (attachment.hasBasicAuthentication()) {
            HttpHandler httpHandler = RequestUtils.wrapBasicAuthentication(
                    Application.getInstance(LocaleHandler.class),
                    attachment.getUsername(),
                    attachment.getPassword());
            
            httpHandler.handleRequest(exchange);
        } else {
            Application.getInstance(LocaleHandler.class).handleRequest(exchange);    
        }
    }
}