package io.mangoo.filters;

import io.mangoo.constants.Key;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.Headers;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class OriginFilter implements PerRequestFilter {
    private final String allowedOrigins;

    @Inject
    public OriginFilter(@Named(Key.APPLICATION_ALLOWED_ORIGINS) String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public Response execute(Request request, Response response) {
        String origin = request.getHeader(Headers.ORIGIN);
        if (origin == null || !allowedOrigins.contains(origin)) {
            return Response.forbidden().bodyText("Origin not allowed").end();
        }

        return response;
    }
}

