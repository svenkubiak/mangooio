package io.mangoo.filters;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.mangoo.constants.Key;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.paseto.PasetoParser;

import java.time.LocalDateTime;

public class PasetoFilter implements PerRequestFilter {
    @Inject
    @Named(Key.PASETO_SECRET)
    private String secret;

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
