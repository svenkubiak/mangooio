package io.mangoo.routing.handlers;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.routing.RequestAttachment;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 *
 * @author svenkubiak
 * @author William Dunne
 *
 */
public class LocaleHandler implements HttpHandler {
    private Config config;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        config = Application.getInstance(Config.class);

        final RequestAttachment requestAttachment = exchange.getAttachment(RequestUtils.REQUEST_ATTACHMENT);
        final HeaderValues headerValues = exchange.getRequestHeaders().get(Headers.ACCEPT_LANGUAGE_STRING);
        final Cookie localeCookie = exchange.getRequestCookies().getOrDefault(config.getLocaleCookieName(), null);
        Locale locale = Locale.forLanguageTag(requestAttachment.getConfig().getApplicationLanguage());

        if(localeCookie == null) {
            if (headerValues != null) {
                String acceptLanguage = headerValues.element();
                if (StringUtils.isNotBlank(acceptLanguage)) {
                    locale = LocaleUtils.getLocaleFromString(acceptLanguage);
                }
            }
        }
        else {
            locale = LocaleUtils.getLocaleFromString(localeCookie.getValue());
        }

        Locale.setDefault(locale);
        requestAttachment.getMessages().reload();
        nextHandler(exchange);
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    private void nextHandler(HttpServerExchange exchange) throws Exception {
        new InboundCookiesHandler().handleRequest(exchange);
    }
}