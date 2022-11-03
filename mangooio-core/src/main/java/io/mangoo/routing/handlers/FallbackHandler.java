package io.mangoo.routing.handlers;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.core.Server;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.enums.Template;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

public class FallbackHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey().toHttpString(), entry.getValue()));
        
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE.toHttpString(), Default.CONTENT_TYPE.toString());
        exchange.setStatusCode(StatusCodes.NOT_FOUND);
        exchange.getResponseSender().send(Template.DEFAULT.notFound());
    }
}