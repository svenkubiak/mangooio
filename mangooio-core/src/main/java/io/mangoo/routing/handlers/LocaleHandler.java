package io.mangoo.routing.handlers;

import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderValues;
import io.undertow.util.LocaleUtils;

/**
 *
 * @author svenkubiak
 * @author williamdunne
 *
 */
public class LocaleHandler implements HttpHandler {
    private Config config;
    
    @Inject
    public LocaleHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Locale locale = Locale.forLanguageTag(this.config.getApplicationLanguage());
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());

        Cookie i18nCookie = exchange.getRequestCookies().get(this.config.getI18nCookieName());
        if (i18nCookie == null) {
            final HeaderValues headerValues = exchange.getRequestHeaders().get(Header.ACCEPT_LANGUAGE.toHttpString());
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
        attachment.withLocale(locale);
        
        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);
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