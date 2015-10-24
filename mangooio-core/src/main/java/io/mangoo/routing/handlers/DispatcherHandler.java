package io.mangoo.routing.handlers;

import io.mangoo.core.Application;
import io.mangoo.routing.listeners.MetricsListener;
import io.mangoo.utils.ConfigUtils;
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
    private boolean metrics;

    public DispatcherHandler(Class<?> controllerClass, String controllerMethod) {
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
        this.metrics = ConfigUtils.isAdminMetricsEnabled();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (this.metrics) {
            exchange.addResponseCommitListener(Application.getInstance(MetricsListener.class));
        }

        new RequestHandler(this.controllerClass, this.controllerMethod).handleRequest(exchange);
    }
}