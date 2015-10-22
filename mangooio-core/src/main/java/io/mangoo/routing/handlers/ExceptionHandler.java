package io.mangoo.routing.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mangoo.core.Application;
import io.mangoo.enums.ContentType;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.enums.Template;
import io.mangoo.templating.TemplateEngine;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class ExceptionHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);
    private static final String MESSAGE = "Failed to pass and exception to the frontend";
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Throwable throwable = null; 
        try {
            throwable = exchange.getAttachment(io.undertow.server.handlers.ExceptionHandler.THROWABLE);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.TEXT_HTML.toString());
            exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), Default.XSS_PROTECTION.toInt());
            exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), Default.NOSNIFF.toString());
            exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), Default.SAMEORIGIN.toString());
            exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);

            if (Application.inDevMode()) {
                TemplateEngine templateEngine = Application.getInstance(TemplateEngine.class);
                
                if (throwable == null) {
                    exchange.getResponseSender().send(Template.DEFAULT.internalServerError());
                } else if (throwable.getCause() == null) {
                    exchange.getResponseSender().send(templateEngine.renderException(exchange, throwable, true));
                } else {
                    exchange.getResponseSender().send(templateEngine.renderException(exchange, throwable.getCause(), false));
                }
            } else {
                exchange.getResponseSender().send(Template.DEFAULT.internalServerError());
            }
        } catch (Exception e) { //NOSONAR
            if (throwable ==  null) {
                LOG.error(MESSAGE, e);
            } else {
                LOG.error(MESSAGE, throwable);
            }
        }
    }
}