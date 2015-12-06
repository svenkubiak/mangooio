package io.mangoo.routing.handlers;

import java.util.Objects;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.routing.listeners.MetricsListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("all")
public class DispatcherHandler implements HttpHandler {
    private final Class<?> controllerClass;
    private final String controllerMethod;
    private final boolean metrics;
    private final boolean async;

    public DispatcherHandler(Class<?> controllerClass, String controllerMethod, boolean async) {
        Objects.requireNonNull(controllerClass, "controllerClass can not be null");
        Objects.requireNonNull(controllerMethod, "controllerMethod can not be null");

        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
        this.async = async;
        this.metrics = Application.getInstance(Config.class).isAdminMetricsEnabled();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (this.metrics) {
            exchange.addResponseCommitListener(Application.getInstance(MetricsListener.class));
        }

        new RequestHandler(this.controllerClass, this.controllerMethod, this.async).handleRequest(exchange);
    }
}