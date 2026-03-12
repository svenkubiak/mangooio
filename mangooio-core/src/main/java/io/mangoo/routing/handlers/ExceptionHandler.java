package io.mangoo.routing.handlers;

import com.google.common.net.MediaType;
import io.mangoo.constants.Header;
import io.mangoo.constants.Template;
import io.mangoo.core.Application;
import io.mangoo.core.Server;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ExceptionHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(ExceptionHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Throwable throwable = exchange.getAttachment(io.undertow.server.handlers.ExceptionHandler.THROWABLE);

        if (throwable == null) {
            return;
        }

        if (exchange.isResponseStarted()) {
            LOG.error("Response already started, cannot handle exception properly", throwable);
            return;
        }

        var root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }

        int status = resolveStatus(root);
        if (status >= 500) {
            LOG.error("Unhandled server exception", root);
        } else {
            LOG.warn("Client error: {}", root.getMessage());
        }

        Server.headers().forEach((key, value) -> {
            if (StringUtils.isNotBlank(value)) {
                exchange.getResponseHeaders().add(key, value);
            }
        });

        exchange.setStatusCode(status);

        boolean isJson = RequestUtils.isJsonRequest(exchange);
        if (isJson) {
            handleJsonResponse(exchange, status);
        } else {
            handleHtmlResponse(exchange, root, status);
        }
    }

    private int resolveStatus(Throwable root) {
        if (root instanceof IllegalArgumentException) {
            return StatusCodes.BAD_REQUEST;
        }

        if (root instanceof IOException) {
            return StatusCodes.BAD_REQUEST;
        }

        return StatusCodes.INTERNAL_SERVER_ERROR;
    }

    private void handleJsonResponse(HttpServerExchange exchange, int status) {
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE, "application/json");

        String json = "{\"error\":\"" + StatusCodes.getReason(status) + "\"}";
        exchange.getResponseSender().send(json);
    }

    private void handleHtmlResponse(HttpServerExchange exchange, Throwable root, int status) throws MangooTemplateEngineException {
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE, MediaType.HTML_UTF_8.withoutParameters().toString());

        if (Application.inDevMode() && status >= 500) {
            var templateEngine = new TemplateEngine();
            exchange.getResponseSender()
                    .send(templateEngine.renderException(exchange, root, true));
        } else {
            exchange.getResponseSender()
                    .send(Template.internalServerError());
        }
    }
}