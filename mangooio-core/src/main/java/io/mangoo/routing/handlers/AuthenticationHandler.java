package io.mangoo.routing.handlers;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.core.Server;
import io.mangoo.enums.Header;
import io.mangoo.enums.Key;
import io.mangoo.enums.Required;
import io.mangoo.enums.Template;
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
public class AuthenticationHandler implements HttpHandler {
    private final Config config;
    
    @Inject
    public AuthenticationHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        
        if (attachment.hasAuthentication()) {
            var authentication = attachment.getAuthentication();
            
            if (!authentication.isValid() || ( authentication.isValid() && authentication.isTwoFactor() )) {
                var redirect = config.getString(Key.AUTHENTICATION_REDIRECT.toString());
                if (StringUtils.isNotBlank(redirect)) {
                    endRequest(exchange, redirect);
                } else {
                    endRequest(exchange);
                }
            } else {
                nextHandler(exchange);
            }
        } else {
            nextHandler(exchange); 
        }
    }
    
    /**
     * Ends the current request by sending an HTTP 302 status code and a direct to the given URL
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange, String redirect) {
        exchange.setStatusCode(StatusCodes.FOUND);
        
        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey().toHttpString(), entry.getValue()));
        
        exchange.getResponseHeaders().put(Header.LOCATION.toHttpString(), redirect);
        exchange.endExchange();
    }
    
    /**
     * Ends the current request by sending an HTTP 403 status code and the default forbidden template
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.FORBIDDEN);
        
        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey().toHttpString(), entry.getValue()));
        
        exchange.getResponseSender().send(Template.DEFAULT.forbidden());
    }
    
    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(FormHandler.class).handleRequest(exchange);

    }
}