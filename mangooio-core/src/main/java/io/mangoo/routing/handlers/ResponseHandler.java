package io.mangoo.routing.handlers;

import io.mangoo.constants.Header;
import io.mangoo.core.Application;
import io.mangoo.core.Server;
import io.mangoo.routing.Response;
import io.mangoo.utils.FileUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class ResponseHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(ResponseHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        var attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        final var response = attachment.getResponse();

        if (response.isRedirect()) {
            handleRedirectResponse(exchange, response);
        } else if (response.isFile()) {
            handleFileResponse(exchange, response);
        } else if (response.isBinary()) {
            handleBinaryResponse(exchange, response);
        } else {
            handleRenderedResponse(exchange, response);
        }
        
        var form = attachment.getForm();
        if (form != null) {
            form.discard();
        }

        // No Trace.end() here. Sending the response completes the exchange, which fires the
        // completion listener that closes the remaining spans. Ending the span at this point
        // would always come too late and is also unnecessary, as closing it from the listener
        // covers an aborted request the same way it covers this one.
    }

    /**
     * Handles a binary response to the client by sending the binary content from the response
     * to the undertow output stream
     *
     * @param exchange The Undertow HttpServerExchange
     * @param response The response object
     */
    protected void handleBinaryResponse(HttpServerExchange exchange, Response response) {
        exchange.dispatch(exchange.getDispatchExecutor(), ExceptionHandler.wrap(Application.getInstance(BinaryHandler.class).withResponse(response)));
    }

    /**
     * Handles a file response to the client by transferring the file from a FileChannel
     * to the undertow response sender. The file is never read into the heap, the memory
     * usage of the response is therefore independent of the size of the file.
     *
     * The exchange is intentionally not switched to blocking mode, so that the sender
     * transfers the file asynchronously and the thread is not occupied for the duration
     * of the transfer.
     *
     * @param exchange The Undertow HttpServerExchange
     * @param response The response object
     */
    protected void handleFileResponse(HttpServerExchange exchange, Response response) {
        exchange.setStatusCode(response.getStatusCode());

        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey(), entry.getValue()));

        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().put(key, value));

        var path = response.getFileBody();
        final FileChannel fileChannel;
        try {
            // Only an explicitly set content type ends up in the response headers, the content
            // type of the response object itself is prefilled with a default and is therefore
            // no indication that the developer has chosen a content type
            if (!response.getHeaders().containsKey(Header.CONTENT_TYPE)) {
                try (var inputStream = Files.newInputStream(path)) {
                    String mimeType = FileUtils.getMimeType(inputStream);
                    if (StringUtils.isNotBlank(mimeType)) {
                        exchange.getResponseHeaders().put(Header.CONTENT_TYPE, mimeType);
                    }
                }
            }

            exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, Files.size(path));
            fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        } catch (IOException e) {
            LOG.error("Failed to send file response", e);
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.endExchange();
            return;
        }

        exchange.getResponseSender().transferFrom(fileChannel, new IoCallback() {
            @Override
            public void onComplete(HttpServerExchange httpServerExchange, Sender sender) {
                FileUtils.closeQuietly(fileChannel);
                IoCallback.END_EXCHANGE.onComplete(httpServerExchange, sender);
            }

            @Override
            public void onException(HttpServerExchange httpServerExchange, Sender sender, IOException exception) {
                FileUtils.closeQuietly(fileChannel);
                IoCallback.END_EXCHANGE.onException(httpServerExchange, sender, exception);
            }
        });
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
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().put(key, value));
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
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().put(key, value));
        exchange.getResponseSender().send(response.getBody());
    }
}