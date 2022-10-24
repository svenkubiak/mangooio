package io.mangoo.routing.handlers;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.core.Application;
import io.mangoo.core.Server;
import io.mangoo.enums.Header;
import io.mangoo.routing.Response;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class ResponseHandler implements HttpHandler {
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        var attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        final var response = attachment.getResponse();

        if (response.isRedirect()) {
            handleRedirectResponse(exchange, response);
        } else if (response.isBinary()) {
            handleBinaryResponse(exchange, response);
        } else {
            handleRenderedResponse(exchange, response);
        }
        
        var form = attachment.getForm();
        if (form != null) {
            form.discard();
        }
    }

    /**
     * Handles a binary response to the client by sending the binary content from the response
     * to the undertow output stream
     *
     * @param exchange The Undertow HttpServerExchange
     * @param response The response object
     */
    protected void handleBinaryResponse(HttpServerExchange exchange, Response response) {
        exchange.dispatch(exchange.getDispatchExecutor(), Application.getInstance(BinaryHandler.class).withResponse(response));
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
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey().toHttpString(), entry.getValue()));

        exchange.getResponseHeaders().put(Header.LOCATION.toHttpString(), response.getRedirectTo());
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
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey().toHttpString(), entry.getValue()));
        
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE.toHttpString(), response.getContentType() + "; charset=" + response.getCharset());
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.getResponseSender().send(response.getBody());
    }
}