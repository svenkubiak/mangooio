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

        if (headerValues == null) {
            Locale.setDefault(Locale.forLanguageTag(requestAttachment.getConfig().getApplicationLanguage()));
        } else if (headerValues.getFirst() != null) {
            final String values = Optional.ofNullable(headerValues.getFirst()).orElse("");
            final Iterable<String> split = Splitter.on(",").trimResults().split(values);
            if (split == null) {
                Locale.setDefault(Locale.forLanguageTag(requestAttachment.getConfig().getApplicationLanguage()));
            } else {
                final String acceptLanguage = Optional.ofNullable(split.iterator().next()).orElse(requestAttachment.getConfig().getApplicationLanguage());
                Locale.setDefault(Locale.forLanguageTag(acceptLanguage.substring(0, 2))); //NOSONAR
            }
        }

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