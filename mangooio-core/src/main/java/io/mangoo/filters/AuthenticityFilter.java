package io.mangoo.filters;

import io.mangoo.enums.Template;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

/**
 * Filter that checks the authenticity token
 *
 * @author svenkubiak
 *
 */
public class AuthenticityFilter implements MangooFilter {
    @Override
    public Response execute(Request request, Response response) {
        if (!request.authenticityMatches()) {
            return Response.withForbidden()
                    .andBody(Template.DEFAULT.forbidden())
                    .end();
        }

        return response;
    }
}