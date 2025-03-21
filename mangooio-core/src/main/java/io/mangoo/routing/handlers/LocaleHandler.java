package io.mangoo.routing.handlers;

import io.mangoo.constants.Default;
import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.LocaleUtils;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LocaleHandler implements HttpHandler {
    private final Config config;
    
    @Inject
    public LocaleHandler(Config config) {
        this.config = Objects.requireNonNull(config, NotNull.CONFIG);
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Locale locale = null;

        var i18nCookie = exchange.getRequestCookie(config.getI18nCookieName());
        Map<String, String> parameter = RequestUtils.getRequestParameters(exchange);
        String lang = parameter.get("lang");

        if (StringUtils.isNotBlank(lang)) {
            locale = LocaleUtils.getLocaleFromString(lang.toLowerCase());
        } else if (i18nCookie != null) {
            locale = LocaleUtils.getLocaleFromString(i18nCookie.getValue().toLowerCase());
        } else {
            var headerValues = exchange.getRequestHeaders().get(Header.ACCEPT_LANGUAGE);
            if (headerValues != null) {
                String acceptLanguage = headerValues.element();
                if (StringUtils.isNotBlank(acceptLanguage)) {
                    locale = LocaleUtils.getLocaleFromString(acceptLanguage.toLowerCase());
                }
            }
        }

        if (locale == null) {
            locale = Locale.forLanguageTag(config.getApplicationLanguage().toLowerCase());
        }

        if (locale == null) {
            locale = Locale.forLanguageTag(Default.LANGUAGE);
        }

        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
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
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(InboundCookiesHandler.class).handleRequest(exchange);
    }
}