package io.mangoo.routing.handlers;

import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
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
    private Config config;
    private Response response;

    @Inject
    public BinaryHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    public BinaryHandler withResponse(Response response) {
        if (this.response == null) {
            this.response = Objects.requireNonNull(response, Required.RESPONSE.toString());
        }
        
        return this;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.startBlocking();
        exchange.setStatusCode(this.response.getStatusCode());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.toString());
        exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "inline; filename=" + this.response.getBinaryFileName());
        exchange.getResponseHeaders().put(Headers.SERVER, this.config.getServerHeader());
        this.response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value)); // NOSONAR
        exchange.getOutputStream().write(this.response.getBinaryContent());
    }
}