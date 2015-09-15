package io.mangoo.routing.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("all")
public class DispatcherHandler implements HttpHandler {
    private Class<?> controllerClass;
    private String controllerMethod;

    public DispatcherHandler(Class<?> controllerClass, String controllerMethod) {
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        RequestHandler requestHandler = new RequestHandler(this.controllerClass, this.controllerMethod);
        requestHandler.handleRequest(exchange);
    }
}