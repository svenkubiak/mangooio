package mangoo.io.filters;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import mangoo.io.configuration.Config;
import mangoo.io.enums.ContentType;
import mangoo.io.enums.Key;
import mangoo.io.enums.Template;
import mangoo.io.interfaces.MangooControllerFilter;
import mangoo.io.routing.bindings.Exchange;

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
            String redirect = this.config.getString(Key.AUTH_REDIRECT_URL.toString());
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