package io.mangoo.routing.handlers;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.net.MediaType;

import io.mangoo.core.Application;
import io.mangoo.core.Server;
import io.mangoo.enums.Header;
import io.mangoo.enums.Template;
import io.mangoo.templating.TemplateEngine;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class ExceptionHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(ExceptionHandler.class);
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Throwable throwable = exchange.getAttachment(io.undertow.server.handlers.ExceptionHandler.THROWABLE);
        if (throwable != null) {
            LOG.error("Internal Server Exception", throwable);
        }
        
        try {
            Server.headers()
                .entrySet()
                .stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey(), entry.getValue()));

            exchange.getResponseHeaders().put(Header.CONTENT_TYPE.toHttpString(), MediaType.HTML_UTF_8.withoutParameters().toString());
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);

            if (Application.inDevMode()) {
                TemplateEngine templateEngine = new TemplateEngine();
                if (throwable == null) {
                    exchange.getResponseSender().send(Template.DEFAULT.serverError());
                } else if (throwable.getCause() == null) {
                    exchange.getResponseSender().send(templateEngine.renderException(exchange, throwable, true));
                } else {
                    exchange.getResponseSender().send(templateEngine.renderException(exchange, throwable.getCause(), false));
                }
            } else {
                exchange.getResponseSender().send(Template.DEFAULT.serverError());
            }
        } catch (Exception e) { // NOSONAR Intentionally catching Exception
            LOG.error("Failed to pass an exception to the frontend", e); 
        }
    }
}