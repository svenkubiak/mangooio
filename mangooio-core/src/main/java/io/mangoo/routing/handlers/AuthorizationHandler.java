package io.mangoo.routing.handlers;

import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.services.AuthorizationService;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthorizationHandler implements HttpHandler {
    private Config config;
    private AuthorizationService authorizationService;
    
    @Inject
    public AuthorizationHandler(Config config, AuthorizationService authorizationService) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
        this.authorizationService = Objects.requireNonNull(authorizationService, Required.AUTHORIZATION_SERVICE.toString());
    }
    
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        
        if (attachment.hasAuthorization()) {
            Authentication authentication = attachment.getAuthentication();
            
            if (authentication != null && authentication.isValid()) {
                String subject = null;
                String resource = null;
                String operation = null;
                
                if (!this.authorizationService.validAuthorization(subject, resource, operation)) {
                    endRequest(exchange);
                } else {
                    nextHandler(exchange);
                }
            } else {
                nextHandler(exchange);            
            }
        } else {
            nextHandler(exchange); 
        }
    }
    
    /**
     * Ends the current request by sending a HTTP 403 status code and the default unauthorized template
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
        exchange.getResponseHeaders().put(Header.SERVER.toHttpString(), this.config.getApplicationHeadersServer());
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
        Application.getInstance(FormHandler.class).handleRequest(exchange);
    }
}