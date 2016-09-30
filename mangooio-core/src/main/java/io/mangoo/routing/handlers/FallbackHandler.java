package io.mangoo.routing.handlers;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.enums.Template;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class FallbackHandler implements HttpHandler {
    private static final Config CONFIG = Application.getConfig();
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), CONFIG.getXssProectionHeader());
        exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), CONFIG.getXContentTypeOptionsHeader());
        exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), CONFIG.getXFrameOptionsHeader());
        exchange.getResponseHeaders().put(Headers.SERVER, CONFIG.getServerHeader());
        exchange.getResponseHeaders().put(Header.CONTENT_SECURITY_POLICY.toHttpString(), CONFIG.getContentSecurityPolicyHeader());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, Default.CONTENT_TYPE.toString());
        exchange.setStatusCode(StatusCodes.NOT_FOUND);
        exchange.getResponseSender().send(Template.DEFAULT.notFound());
    }
}