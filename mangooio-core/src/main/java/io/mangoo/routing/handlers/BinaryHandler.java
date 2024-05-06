package io.mangoo.routing.handlers;

import com.google.common.net.MediaType;
import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Server;
import io.mangoo.routing.Response;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class BinaryHandler implements HttpHandler {
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

        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey(), entry.getValue()));
        
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE, MediaType.OCTET_STREAM.withoutParameters().toString());
        exchange.getResponseHeaders().put(Header.CONTENT_DISPOSITION, "inline; filename=" + response.getBinaryFileName());
        this.response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.getOutputStream().write(this.response.getBinaryContent());
    }
}