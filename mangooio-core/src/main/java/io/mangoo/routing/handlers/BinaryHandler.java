package io.mangoo.routing.handlers;

import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Server;
import io.mangoo.routing.Response;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;

import java.util.Objects;

public class BinaryHandler implements HttpHandler {
    private final Tika tika = new Tika();
    private Response response;

    public BinaryHandler withResponse(Response response) {
        if (this.response == null) {
            this.response = Objects.requireNonNull(response, NotNull.RESPONSE);
        }

        return this;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.startBlocking();
        exchange.setStatusCode(response.getStatusCode());

        String mimeType = tika.detect(response.getBinaryBody());
        if (StringUtils.isNotBlank(mimeType)) {
            exchange.getResponseHeaders().put(Header.CONTENT_TYPE, mimeType);
        }

        Server.headers()
                .entrySet()
                .stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey(), entry.getValue()));

        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.getOutputStream().write(response.getBinaryBody());
    }
}