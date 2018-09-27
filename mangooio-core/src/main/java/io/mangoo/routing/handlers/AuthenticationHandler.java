package io.mangoo.routing.handlers;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Key;
import io.mangoo.enums.Required;
import io.mangoo.enums.Template;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
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
    private Config config;
    
    @Inject
    public AuthenticationHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        
        if (attachment.hasAuthentication()) {
            Authentication authentication = attachment.getAuthentication();
            
            if (!authentication.isValid() || ( authentication.isValid() && authentication.isTwoFactor() )) {
                String redirect = this.config.getString(Key.AUTHENTICATION_REDIRECT.toString());
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
     * Ends the current request by sending a HTTP 302 status code and a direct to the given URL
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange, String redirect) {
        exchange.setStatusCode(StatusCodes.FOUND);
        exchange.getResponseHeaders().put(Header.LOCATION.toHttpString(), redirect);
        exchange.getResponseHeaders().put(Header.SERVER.toHttpString(), this.config.getApplicationHeadersServer());
        exchange.endExchange();
    }
    
    /**
     * Ends the current request by sending a HTTP 403 status code and the default forbidden template
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.FORBIDDEN);
        exchange.getResponseHeaders().put(Header.SERVER.toHttpString(), this.config.getApplicationHeadersServer());
        exchange.getResponseSender().send(Template.DEFAULT.forbidden());
    }
    
    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(AuthorizationHandler.class).handleRequest(exchange);
    }
}