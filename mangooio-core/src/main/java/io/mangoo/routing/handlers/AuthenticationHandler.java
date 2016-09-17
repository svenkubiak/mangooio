package io.mangoo.routing.handlers;

import io.mangoo.core.Application;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthenticationHandler implements HttpHandler {
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        nextHandler(exchange);
    }
    
    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(LocaleHandler.class).handleRequest(exchange);
    }
}