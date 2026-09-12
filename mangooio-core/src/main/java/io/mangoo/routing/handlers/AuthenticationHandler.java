package io.mangoo.routing.handlers;

import io.mangoo.constants.Header;
import io.mangoo.constants.Key;
import io.mangoo.constants.Required;
import io.mangoo.constants.Template;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.core.Server;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AuthenticationHandler implements HttpHandler {
    private static final String ORIGIN_PARAMETER = "origin";
    private final Config config;
    
    @Inject
    public AuthenticationHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG);
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        
        if (attachment.hasAuthentication()) {
            var authentication = attachment.getAuthentication();
            
            if (!authentication.isValid()) {
                var redirect = config.getString(Key.AUTHENTICATION_REDIRECT);
                if (StringUtils.isNotBlank(redirect)) {
                    endRequest(exchange, redirect);
                } else {
                    endRequest(exchange);
                }
            } else if (authentication.isValid() && authentication.isTwoFactor()) {
                var redirect = config.getString(Key.AUTHENTICATION_REDIRECT_MFA, config.getString(Key.AUTHENTICATION_REDIRECT));
                if (StringUtils.isNotBlank(redirect)) {
                    endRequest(exchange, redirect);
                } else {
                    endRequest(exchange);
                }
            } else {
                nextHandler(exchange);
            }
        } else {
            nextHandler(exchange); 
        }
    }
    
    /**
     * Ends the current request by sending an HTTP 302 status code and a direct to the given URL
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange, String redirect) {
        exchange.setStatusCode(StatusCodes.FOUND);
        
        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().put(entry.getKey(), entry.getValue()));

        if (config.isAuthOrigin()) {
            redirect = redirect + (redirect.indexOf('?') == -1 ? '?' : '&') + ORIGIN_PARAMETER + '=' + origin(exchange);
        }
        exchange.getResponseHeaders().put(Header.LOCATION,redirect);
        exchange.endExchange();
    }

    /**
     * Creates the value of the origin parameter for the given request, consisting of the
     * request URI and, if present, the query string.
     *
     * Leading slashes are collapsed into a single one, so that the value can not be read as a
     * protocol-relative URL pointing at a foreign host, and the result is URL encoded, so that
     * it can not inject additional parameters into the redirect.
     *
     * @param exchange The HttpServerExchange
     * @return The encoded value of the origin parameter
     */
    static String origin(HttpServerExchange exchange) {
        var origin = StringUtils.defaultString(exchange.getRequestURI());
        var queryString = exchange.getQueryString();

        if (StringUtils.isNotBlank(queryString)) {
            origin = origin + '?' + queryString;
        }

        return URLEncoder.encode('/' + StringUtils.stripStart(origin, "/"), StandardCharsets.UTF_8);
    }
    
    /**
     * Ends the current request by sending an HTTP 403 status code and the default forbidden template
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.FORBIDDEN);
        
        Server.headers()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> exchange.getResponseHeaders().add(entry.getKey(), entry.getValue()));

        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/html; charset=utf-8");
        exchange.getResponseSender().send(Template.unauthorized());
    }
    
    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(FormHandler.class).handleRequest(exchange);
    }
}