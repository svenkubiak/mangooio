package io.mangoo.routing.handlers;

import jakarta.inject.Inject;
import io.mangoo.constants.Header;
import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.constants.Template;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.core.Server;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class AuthenticationHandler implements HttpHandler {
    private final Config config;
    
    @Inject
    public AuthenticationHandler(Config config) {
        this.config = Objects.requireNonNull(config, NotNull.CONFIG);
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        
        if (attachment.hasAuthentication()) {
            var authentication = attachment.getAuthentication();
            
            if (!authentication.isValid() || ( authentication.isValid() && authentication.isTwoFactor() )) {
                var redirect = config.getString(Key.AUTHENTICATION_REDIRECT);
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
            .forEach(entry -> exchange.getResponseHeaders().put(entry.getKey(), entry.getValue()));

        if (config.isAuthOrigin()) {
            redirect = redirect + "?origin=" + exchange.getRequestURI();
        }
        exchange.getResponseHeaders().put(Header.LOCATION,redirect);
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
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey(), entry.getValue()));
        
        exchange.getResponseSender().send(Template.unauthorized());
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