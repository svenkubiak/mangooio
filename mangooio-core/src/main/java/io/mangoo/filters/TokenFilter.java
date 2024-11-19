package io.mangoo.filters;

import com.google.inject.Inject;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.paseto.PasetoParser;
import io.mangoo.utils.paseto.Token;
import io.undertow.util.HttpString;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

public class TokenFilter implements PerRequestFilter {
    private final Config config;

    @Inject
    public TokenFilter(Config config) {
        this.config = Objects.requireNonNull(config, NotNull.CONFIG);
    }

    @Override
    public Response execute(Request request, Response response) {
        String authorization = request.getHeader(new HttpString("Authorization"));

        if (StringUtils.isNotBlank(authorization)) {
            try {
                Token token = PasetoParser.create()
                        .withValue(authorization)
                        .withSecret(config.getTokenSecret())
                        .parse();

                if (token != null && token.getExpires().isAfter(LocalDateTime.now())) {
                    return response;
                }
            } catch (MangooTokenException e) {
                //intentionally left blank
            }
        }

        return Response.unauthorized().end();
    }
}
