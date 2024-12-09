package io.mangoo.filters;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import io.mangoo.constants.Key;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.RequestUtils;
import org.apache.commons.lang3.StringUtils;

public class ApiKeyFilter implements PerRequestFilter {
    @Inject
    @Named(Key.API_KEY)
    private String key;

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

