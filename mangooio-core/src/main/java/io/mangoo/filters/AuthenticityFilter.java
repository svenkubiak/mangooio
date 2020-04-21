package io.mangoo.filters;

import io.mangoo.enums.Template;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

/**
 * Filter that checks the authenticity token
 *
 * @author svenkubiak
 *
 */
public class AuthenticityFilter implements PerRequestFilter {
    @Override
    public Response execute(Request request, Response response) {
        if (!request.authenticityMatches()) {
            return Response.withForbidden()
                    .andBody(Template.DEFAULT.forbidden())
                    .andEndResponse();
        }

        return response;
    }
}