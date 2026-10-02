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

        // No Trace.end() here: completing the exchange fires the completion listener, which closes
        // the remaining spans and also covers aborted requests.
    }

        protected void handleBinaryResponse(HttpServerExchange exchange, Response response) {
        exchange.dispatch(exchange.getDispatchExecutor(), ExceptionHandler.wrap(Application.getInstance(BinaryHandler.class).withResponse(response)));
    }

    // Transferred from a FileChannel without blocking, so the file is never read into the heap.
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
            // Only an explicitly set header counts, as the content type of the response object is always prefilled with a default.
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