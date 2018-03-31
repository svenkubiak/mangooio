package io.mangoo.routing.handlers;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.net.MediaType;
import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.enums.Template;
import io.mangoo.interfaces.MangooTemplateEngine;
import io.mangoo.templating.TemplateEngineFreemarker;
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
    private Config config;
    
    @Inject
    public ExceptionHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Throwable throwable = exchange.getAttachment(io.undertow.server.handlers.ExceptionHandler.THROWABLE);
        if (throwable != null) {
            LOG.error("Internal server exception", throwable);
        }
        
        try {
            exchange.getResponseHeaders().put(Header.CONTENT_TYPE.toHttpString(), MediaType.HTML_UTF_8.withoutParameters().toString());
            exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), this.config.getApplicationHeaderXssProection());
            exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), this.config.getApplicationHeadersXContentTypeOptions());
            exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), this.config.getApplicationHeadersXFrameOptions());
            exchange.getResponseHeaders().put(Header.CONTENT_SECURITY_POLICY.toHttpString(), this.config.getApplicationHeadersContentSecurityPolicy());
            exchange.getResponseHeaders().put(Header.SERVER.toHttpString(), this.config.getApplicationHeadersServer());
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);

            if (Application.inDevMode()) {
                MangooTemplateEngine templateEngine = new TemplateEngineFreemarker();
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
        } catch (Exception e) { //NOSONAR
            LOG.error("Failed to pass an exception to the frontend", e);
        }
    }
}