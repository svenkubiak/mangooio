package mangoo.io.routing.handlers;

import com.google.inject.Injector;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;

/**
 *
 * @author svenkubiak
 *
 */
public class DispatcherHandler implements HttpHandler {
    private static final AttachmentKey<Throwable> THROWABLE = AttachmentKey.create(Throwable.class);
    private Class<?> controllerClass;
    private String controllerMethod;
    private Injector injector;

    public DispatcherHandler(Class<?> controllerClass, String controllerMethod, Injector injector) {
        this.injector = injector;
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            exchange.dispatch(exchange.getDispatchExecutor(), new RequestHandler(this.controllerClass, this.controllerMethod, this.injector));
        } catch (Exception e) {
            exchange.putAttachment(THROWABLE, e);
            throw new Exception();
        }
    }
}