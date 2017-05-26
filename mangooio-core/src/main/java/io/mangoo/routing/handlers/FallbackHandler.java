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
        exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), this.config.getXssProectionHeader());
        exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), this.config.getXContentTypeOptionsHeader());
        exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), this.config.getXFrameOptionsHeader());
        exchange.getResponseHeaders().put(Header.SERVER.toHttpString(), this.config.getServerHeader());
        exchange.getResponseHeaders().put(Header.CONTENT_SECURITY_POLICY.toHttpString(), this.config.getContentSecurityPolicyHeader());
        exchange.getResponseHeaders().put(Header.CONTENT_TYPE.toHttpString(), Default.CONTENT_TYPE.toString());
        exchange.setStatusCode(StatusCodes.NOT_FOUND);
        exchange.getResponseSender().send(Template.DEFAULT.notFound());
    }
}