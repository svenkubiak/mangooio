package io.mangoo.filters;

import io.mangoo.constants.Key;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.Headers;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class OriginFilter implements PerRequestFilter {
    private final Set<String> allowedOrigins;

    @Inject
    public OriginFilter(@Named(Key.APPLICATION_ALLOWED_ORIGINS) String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public Response execute(Request request, Response response) {
        String origin = request.getHeader(Headers.ORIGIN);
        if (origin == null || !allowedOrigins.contains(origin.trim().toLowerCase())) {
            return Response.forbidden().end();
        }

        return response;
    }
}

