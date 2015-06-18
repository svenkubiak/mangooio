package mangoo.io.routing.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import mangoo.io.enums.Default;
import mangoo.io.enums.Templates;

/**
 *
 * @author svenkubiak
 *
 */
public class ExceptionHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        exchange.setResponseCode(StatusCodes.INTERNAL_SERVER_ERROR);
        exchange.getResponseSender().send(Templates.DEFAULT.internalServerError());
    }
}