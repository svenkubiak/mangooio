package io.mangoo.filters;

import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class CsrfFilter implements PerRequestFilter {
    @Override
    public Response execute(Request request, Response response) {
        if (!request.hasValidCsrf()) {
            return Response.forbidden().end();
        }

        return response;
    }
}