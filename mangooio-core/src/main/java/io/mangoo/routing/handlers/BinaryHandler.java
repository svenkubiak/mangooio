package io.mangoo.routing.handlers;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.common.net.MediaType;

import io.mangoo.core.Server;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.routing.Response;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 */
public class BinaryHandler implements HttpHandler {
    private Response response;

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

        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey().toHttpString(), entry.getValue()));
        
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE.toHttpString(), MediaType.OCTET_STREAM.withoutParameters().toString());
        exchange.getResponseHeaders().put(Header.CONTENT_DISPOSITION.toHttpString(), "inline; filename=" + this.response.getBinaryFileName());        
        this.response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.getOutputStream().write(this.response.getBinaryContent());
    }
}