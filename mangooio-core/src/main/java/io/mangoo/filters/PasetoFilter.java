package io.mangoo.filters;

import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.paseto.PasetoParser;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.time.LocalDateTime;
import java.util.Objects;

public class PasetoFilter implements PerRequestFilter {
    private final String secret;

    @Inject
    public PasetoFilter(@Named(Key.APPLICATION_PASETO_SECRET) String secret) {
        this.secret = Objects.requireNonNull(secret, NotNull.SECRET);
    }

    @Override
    public Response execute(Request request, Response response) {
        return RequestUtils.getAuthorizationHeader(request)
                .map(authorization -> authorize(authorization, secret, response))
                .orElseGet(() -> Response.unauthorized().end());
    }

    private Response authorize(String authorization, String secret, Response response) {
        try {
            var token = PasetoParser.create()
                    .withValue(authorization)
                    .withSecret(secret)
                    .parse();

            if (token != null && token.getExpires().isAfter(LocalDateTime.now())) {
                return response;
            }
        } catch (MangooTokenException e) {
            // Intentionally left blank
        }

        return Response.unauthorized().end();
    }
}
