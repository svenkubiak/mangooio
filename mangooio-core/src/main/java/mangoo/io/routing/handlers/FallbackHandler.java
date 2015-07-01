package mangoo.io.routing.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import mangoo.io.enums.Default;
import mangoo.io.enums.Template;

/**
 *
 * @author svenkubiak
 *
 */
public class FallbackHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, Default.CONTENT_TYPE.toString());
        exchange.setResponseCode(StatusCodes.NOT_FOUND);
        exchange.getResponseSender().send(Template.DEFAULT.notFound());
    }
}