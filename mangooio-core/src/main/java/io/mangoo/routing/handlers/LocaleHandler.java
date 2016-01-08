package io.mangoo.routing.handlers;

import java.util.Locale;
import java.util.Optional;

import com.google.common.base.Splitter;

import io.mangoo.routing.RequestAttachment;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

/**
 *
 * @author svenkubiak
 *
 */
public class LocaleHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final RequestAttachment requestAttachment = exchange.getAttachment(RequestUtils.REQUEST_ATTACHMENT);
        final HeaderValues headerValues = exchange.getRequestHeaders().get(Headers.ACCEPT_LANGUAGE_STRING);

        Locale locale = null;
        String defaultLanguage = requestAttachment.getConfig().getApplicationLanguage();
        if (headerValues == null) {
            locale = Locale.forLanguageTag(defaultLanguage);
        } else if (headerValues.getFirst() != null) {
            final String values = Optional.ofNullable(headerValues.getFirst()).orElse("");
            final Iterable<String> splitter = Splitter.on(",").trimResults().split(values);
            if (splitter == null) {
                locale = Locale.forLanguageTag(defaultLanguage);
            } else {
                final String acceptLanguage = Optional.ofNullable(splitter.iterator().next()).orElse(defaultLanguage);
                locale = Locale.forLanguageTag(acceptLanguage.substring(0, 2)); //NOSONAR
            }
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