package io.mangoo.filters;

import io.mangoo.enums.Template;
import io.mangoo.interfaces.MangooControllerFilter;
import io.mangoo.routing.bindings.Exchange;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class AuthenticityFilter implements MangooControllerFilter {

    @Override
    public boolean filter(Exchange exchange) {
        if (!exchange.authenticityMatches()) {
            exchange.getHttpServerExchange().setResponseCode(StatusCodes.FORBIDDEN);
            exchange.getHttpServerExchange().getResponseSender().send(Template.DEFAULT.forbidden());

            return false;
        }

        return true;
    }
}