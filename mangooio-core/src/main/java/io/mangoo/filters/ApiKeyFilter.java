package io.mangoo.filters;

import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.RequestUtils;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class ApiKeyFilter implements PerRequestFilter {
    private final String key;

    @Inject
    public ApiKeyFilter(@Named(Key.APPLICATION_API_KEY) String key) {
        this.key = Objects.requireNonNull(key, NotNull.KEY);
    }

    @Override
    public Response execute(Request request, Response response) {
        return RequestUtils.getAuthorizationHeader(request)
                .filter(auth -> authorize(auth, key))
                .map(auth -> response)
                .orElse(Response.unauthorized().end());
    }

    private boolean authorize(String authorization, String key) {
        return StringUtils.isNotBlank(authorization) &&
               StringUtils.isNotBlank(key) &&
               authorization.equals(key);
    }
}

