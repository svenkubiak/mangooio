package io.mangoo.filters;

import io.mangoo.enums.Template;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class AuthenticityFilter implements MangooFilter {

    @Override
    public boolean continueRequest(Request request) {
        if (!request.authenticityMatches()) {
            request.getHttpServerExchange().setResponseCode(StatusCodes.FORBIDDEN);
            request.getHttpServerExchange().getResponseSender().send(Template.DEFAULT.forbidden());

            return false;
        }

        return true;
    }
}