package io.mangoo.routing.handlers;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.Response;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class ResponseHandler implements HttpHandler {
    private Attachment attachment;
    private Config config;
    
    @Inject
    public ResponseHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.attachment = exchange.getAttachment(RequestUtils.ATTACHMENT_KEY);
        final Response response = this.attachment.getResponse();

        if (response.isRedirect()) {
            handleRedirectResponse(exchange, response);
        } else if (response.isBinary()) {
            handleBinaryResponse(exchange, response);
        } else {
            handleRenderedResponse(exchange, response);
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
    protected void handleBinaryResponse(HttpServerExchange exchange, Response response) throws IOException {
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
        exchange.getResponseHeaders().put(Headers.LOCATION, response.getRedirectTo());
        exchange.getResponseHeaders().put(Headers.SERVER, this.config.getServerHeader());
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value)); //NOSONAR
        exchange.endExchange();
    }

    /**
     * Retrieves the body of the request and checks i an ETag needs to be handled
     *
     * @param exchange The HttpServerExchange
     * @param response The Response object
     * @return The body from the response object or an empty body if etag matches NONE_MATCH header
     */
    protected String getResponseBody(HttpServerExchange exchange, Response response) {
        String responseBody = response.getBody();
        if (response.isETag()) {
            final String noneMatch = exchange.getRequestHeaders().getFirst(Headers.IF_NONE_MATCH_STRING);
            final String etag = DigestUtils.md5Hex(responseBody); //NOSONAR
            if (StringUtils.isNotBlank(noneMatch) && StringUtils.isNotBlank(etag) && noneMatch.equals(etag)) {
                exchange.setStatusCode(StatusCodes.NOT_MODIFIED);
                responseBody = "";
            } else {
                exchange.getResponseHeaders().put(Headers.ETAG, etag);
            }
        }

        return responseBody;
    }

    /**
     * Handles a rendered response to the client by sending the rendered body from the response object
     *
     * @param exchange The Undertow HttpServerExchange
     * @param response The response object
     */
    protected void handleRenderedResponse(HttpServerExchange exchange, Response response) {
        exchange.setStatusCode(response.getStatusCode());
        exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), this.config.getXssProectionHeader());
        exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), this.config.getXContentTypeOptionsHeader());
        exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), this.config.getXFrameOptionsHeader());
        exchange.getResponseHeaders().put(Header.REFERER_POLICY.toHttpString(), this.config.getRefererPolicy());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, response.getContentType() + "; charset=" + response.getCharset());
        exchange.getResponseHeaders().put(Headers.SERVER, this.config.getServerHeader());
        exchange.getResponseHeaders().put(Header.CONTENT_SECURITY_POLICY.toHttpString(), this.config.getContentSecurityPolicyHeader());
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value)); //NOSONAR

        if (this.attachment.hasTimer()) {
            exchange.getResponseHeaders().put(Header.X_RESPONSE_TIME.toHttpString(), this.attachment.getResponseTime() + " ms");
        }

        exchange.getResponseSender().send(getResponseBody(exchange, response));
    }
}