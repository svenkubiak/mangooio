package mangoo.io.routing.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import mangoo.io.enums.ContentType;
import mangoo.io.enums.Default;
import mangoo.io.routing.Response;

/**
 *
 * @author svenkubiak
 *
 */
public class BinaryHandler implements HttpHandler {
    private Response response;

    public BinaryHandler(Response response) {
        this.response = response;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.startBlocking();
        exchange.setResponseCode(this.response.getStatusCode());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.APPLICATION_OCTETE_STREAM.toString());
        exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "inline; filename=" + this.response.getBinaryFileName());
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        this.response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value)); //NOSONAR
        exchange.getOutputStream().write(this.response.getBinaryContent());
    }
}