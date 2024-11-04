package io.mangoo.routing.handlers;

import io.mangoo.constants.Header;
import io.mangoo.core.Server;
import io.mangoo.routing.Response;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class ResponseHandler implements HttpHandler {
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        var attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        final var response = attachment.getResponse();

        if (response.isRedirect()) {
            handleRedirectResponse(exchange, response);
        } else {
            handleRenderedResponse(exchange, response);
        }
        
        var form = attachment.getForm();
        if (form != null) {
            form.discard();
        }
    }

    /**
     * Handles a redirect response to the client by sending a 403 status code to the client
     *
     * @param exchange The Undertow HttpServerExchange
     * @param response The response object
     */
    protected void handleRedirectResponse(HttpServerExchange exchange, Response response) {
        exchange.setStatusCode(StatusCodes.FOUND);
        
        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey(), entry.getValue()));

        exchange.getResponseHeaders().put(Header.LOCATION, response.getRedirectTo());
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.endExchange();
    }

    /**
     * Handles a rendered response to the client by sending the rendered body from the response object
     *
     * @param exchange The Undertow HttpServerExchange
     * @param response The response object
     */
    protected void handleRenderedResponse(HttpServerExchange exchange, Response response) {
        exchange.setStatusCode(response.getStatusCode());
        
        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey(), entry.getValue()));
        
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE, response.getContentType() + "; charset=" + StandardCharsets.UTF_8.name());
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.getResponseSender().send(response.getBody());
    }
}