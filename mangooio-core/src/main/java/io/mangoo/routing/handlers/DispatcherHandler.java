package io.mangoo.routing.handlers;

import com.google.common.base.Preconditions;

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
    private boolean async;

    public DispatcherHandler(Class<?> controllerClass, String controllerMethod, boolean async) {
        Preconditions.checkNotNull(controllerClass, "controllerClass can not be null");
        Preconditions.checkNotNull(controllerMethod, "controllerMethod can not be null");
        
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
        this.async = async;
        this.metrics = ConfigUtils.isAdminMetricsEnabled();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (this.metrics) {
            exchange.addResponseCommitListener(Application.getInstance(MetricsListener.class));
        }

        new RequestHandler(this.controllerClass, this.controllerMethod, this.async).handleRequest(exchange);
    }
}