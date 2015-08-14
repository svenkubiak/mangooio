package io.mangoo.filters;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.enums.ContentType;
import io.mangoo.enums.Key;
import io.mangoo.enums.Template;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class AuthenticationFilter implements MangooFilter {

    private Config config;

    @Inject
    public AuthenticationFilter (Config config) {
        this.config = Objects.requireNonNull(config, "Config can not be null");
    }

    @Override
    public boolean continueRequest(Request request) {
        if (!request.getAuthentication().hasAuthenticatedUser()) {
            String redirect = this.config.getString(Key.AUTH_REDIRECT.toString());
            if (StringUtils.isNotBlank(redirect)) {
                request.getHttpServerExchange().setResponseCode(StatusCodes.FOUND);
                request.getHttpServerExchange().getResponseHeaders().put(Headers.LOCATION, redirect);
                request.getHttpServerExchange().endExchange();
            } else {
                request.getHttpServerExchange().getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.TEXT_HTML.toString());
                request.getHttpServerExchange().setResponseCode(StatusCodes.UNAUTHORIZED);
                request.getHttpServerExchange().getResponseSender().send(Template.DEFAULT.forbidden());
            }

            return false;
        }

        return true;
    }
}