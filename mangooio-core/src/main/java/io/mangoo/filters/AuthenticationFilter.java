package io.mangoo.filters;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.enums.ContentType;
import io.mangoo.enums.Key;
import io.mangoo.enums.Template;
import io.mangoo.interfaces.MangooControllerFilter;
import io.mangoo.routing.bindings.Exchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class AuthenticationFilter implements MangooControllerFilter{

    @Inject
    private Config config;

    @Override
    public boolean filter(Exchange exchange) {
        if (!exchange.getAuthentication().hasAuthenticatedUser()) {
            String redirect = this.config.getString(Key.AUTH_REDIRECT.toString());
            if (StringUtils.isNotBlank(redirect)) {
                exchange.getHttpServerExchange().setResponseCode(StatusCodes.FOUND);
                exchange.getHttpServerExchange().getResponseHeaders().put(Headers.LOCATION, redirect);
                exchange.getHttpServerExchange().endExchange();
            } else {
                exchange.getHttpServerExchange().getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.TEXT_HTML.toString());
                exchange.getHttpServerExchange().setResponseCode(StatusCodes.UNAUTHORIZED);
                exchange.getHttpServerExchange().getResponseSender().send(Template.DEFAULT.forbidden());
            }

            return false;
        }

        return true;
    }
}