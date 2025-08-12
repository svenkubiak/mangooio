package io.mangoo.filters;

import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.Headers;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Objects;

public class OriginFilter implements PerRequestFilter {
    private final String allowedOrigins;

    @Inject
    public OriginFilter(@Named(Key.APPLICATION_ALLOWED_ORIGINS) String allowedOrigins) {
        this.allowedOrigins = Objects.requireNonNull(allowedOrigins, NotNull.ALLOWED_ORIGINS);
    }

    @Override
    public Response execute(Request request, Response response) {
        String origin = request.getHeader(Headers.ORIGIN);
        if (origin == null || !allowedOrigins.contains(origin)) {
            return Response.forbidden().end();
        }

        return response;
    }
}

