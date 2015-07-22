package io.mangoo.routing.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("all")
public class DispatcherHandler implements HttpHandler {
    private static final AttachmentKey<Throwable> THROWABLE = AttachmentKey.create(Throwable.class);
    private Class<?> controllerClass;
    private String controllerMethod;

    public DispatcherHandler(Class<?> controllerClass, String controllerMethod) {
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            exchange.dispatch(exchange.getDispatchExecutor(), new RequestHandler(this.controllerClass, this.controllerMethod));
        } catch (Exception e) {
            exchange.putAttachment(THROWABLE, e);
            throw new Exception();
        }
    }
}