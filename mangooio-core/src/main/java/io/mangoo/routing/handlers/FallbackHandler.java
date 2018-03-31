package io.mangoo.routing.handlers;

import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.enums.Template;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class FallbackHandler implements HttpHandler {
    private Config config;
    
    @Inject
    public FallbackHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), this.config.getApplicationHeaderXssProection());
        exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), this.config.getApplicationHeadersXContentTypeOptions());
        exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), this.config.getApplicationHeadersXFrameOptions());
        exchange.getResponseHeaders().put(Header.SERVER.toHttpString(), this.config.getApplicationHeadersServer());
        exchange.getResponseHeaders().put(Header.CONTENT_SECURITY_POLICY.toHttpString(), this.config.getApplicationHeadersContentSecurityPolicy());
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE.toHttpString(), Default.CONTENT_TYPE.toString());
        exchange.setStatusCode(StatusCodes.NOT_FOUND);
        exchange.getResponseSender().send(Template.DEFAULT.notFound());
    }
}