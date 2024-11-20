package io.mangoo.filters;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
                .map(authorization -> {
                    if (StringUtils.isNotBlank(authorization) && StringUtils.isNotBlank(key) && authorization.equals(key)) {
                        return response;
                    }
                    return Response.unauthorized().end();
                })
                .orElse(Response.unauthorized().end());
    }
}

