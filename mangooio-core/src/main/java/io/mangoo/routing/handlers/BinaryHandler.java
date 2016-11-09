package io.mangoo.routing.handlers;

import java.util.Objects;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.ContentType;
import io.mangoo.enums.Required;
import io.mangoo.routing.Response;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 *
 * @author svenkubiak
 *
 */
public class BinaryHandler implements HttpHandler {
    private static final Config CONFIG = Application.getConfig();
    private final Response response;

    public BinaryHandler(Response response) {
        this.response = Objects.requireNonNull(response, Required.RESPONSE.toString());
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.startBlocking();
        exchange.setStatusCode(this.response.getStatusCode());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.toString());
        exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "inline; filename=" + this.response.getBinaryFileName());
        exchange.getResponseHeaders().put(Headers.SERVER, CONFIG.getServerHeader());
        this.response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value)); // NOSONAR
        exchange.getOutputStream().write(this.response.getBinaryContent());
    }
}