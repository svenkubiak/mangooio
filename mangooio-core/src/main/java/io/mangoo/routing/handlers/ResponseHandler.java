package io.mangoo.routing.handlers;

import java.io.IOException;
import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Form;
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
    private Config config;
    
    @Inject
    public ResponseHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        final Response response = attachment.getResponse();

        if (response.isRedirect()) {
            handleRedirectResponse(exchange, response);
        } else if (response.isBinary()) {
            handleBinaryResponse(exchange, response);
        } else {
            handleRenderedResponse(exchange, response);
        }
        
        Form form = attachment.getForm();
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
     *
     * @throws IOException
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
        exchange.getResponseHeaders().put(Header.LOCATION.toHttpString(), response.getRedirectTo());
        exchange.getResponseHeaders().put(Header.SERVER.toHttpString(), this.config.getApplicationHeadersServer());
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
        exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), this.config.getApplicationHeaderXssProection());
        exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), this.config.getApplicationHeadersXContentTypeOptions());
        exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), this.config.getApplicationHeadersXFrameOptions());
        exchange.getResponseHeaders().put(Header.REFERER_POLICY.toHttpString(), this.config.getApplicationHeadersRefererPolicy());
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE.toHttpString(), response.getContentType() + "; charset=" + response.getCharset());
        exchange.getResponseHeaders().put(Header.SERVER.toHttpString(), this.config.getApplicationHeadersServer());
        exchange.getResponseHeaders().put(Header.CONTENT_SECURITY_POLICY.toHttpString(), this.config.getApplicationHeadersContentSecurityPolicy());
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.getResponseSender().send(response.getBody());
    }
}