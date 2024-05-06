package io.mangoo.routing.handlers;

import io.mangoo.constants.Default;
import io.mangoo.constants.Header;
import io.mangoo.constants.Template;
import io.mangoo.core.Server;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;

public class FallbackHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey(), entry.getValue()));
        
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE, Default.CONTENT_TYPE);
        exchange.setStatusCode(StatusCodes.NOT_FOUND);
        exchange.getResponseSender().send(Template.notFound());
    }
}