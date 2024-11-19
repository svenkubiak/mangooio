package io.mangoo.filters;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.paseto.PasetoParser;
import io.mangoo.utils.paseto.Token;

import java.time.LocalDateTime;

public class PasetoFilter implements PerRequestFilter {
    @Inject
    @Named("paseto.secret")
    private String secret;

    @Override
    public Response execute(Request request, Response response) {
        return RequestUtils.getAuthorizationHeader(request)
                .map(authorization -> {
                    try {
                        Token token = PasetoParser.create()
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
                })
                .orElse(Response.unauthorized().end());

    }
}
