package io.mangoo.routing.handlers;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.LocaleUtils;

/**
 *
 * @author svenkubiak
 * @author williamdunne
 *
 */
public class LocaleHandler implements HttpHandler {
    private static final Config CONFIG = Application.getConfig();

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Locale locale = Locale.forLanguageTag(CONFIG.getApplicationLanguage());
        Attachment attachment = exchange.getAttachment(RequestUtils.ATTACHMENT_KEY);

        Cookie i18nCookie = exchange.getRequestCookies().get(CONFIG.getI18nCookieName());
        if (i18nCookie == null) {
            final HeaderValues headerValues = exchange.getRequestHeaders().get(Headers.ACCEPT_LANGUAGE_STRING);
            if (headerValues != null) {
                String acceptLanguage = headerValues.element();
                if (StringUtils.isNotBlank(acceptLanguage)) {
                    locale = LocaleUtils.getLocaleFromString(acceptLanguage);
                }
            }
        } else {
            locale = LocaleUtils.getLocaleFromString(i18nCookie.getValue());
        }

        attachment.getMessages().reload(locale);
        nextHandler(exchange);
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(InboundCookiesHandler.class).handleRequest(exchange);
    }
}