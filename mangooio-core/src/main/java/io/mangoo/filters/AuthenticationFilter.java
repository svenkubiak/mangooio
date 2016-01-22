package io.mangoo.filters;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.enums.Template;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

/**
 * Filter for intercepting user authentication
 *
 * @author svenkubiak
 *
 */
public class AuthenticationFilter implements MangooFilter {
    private static final Config CONFIG = Application.getConfig();
    
    @Override
    public Response execute(Request request, Response response) {
        if (!request.getAuthentication().hasAuthenticatedUser()) {
            String redirect = CONFIG.getString(Key.AUTH_REDIRECT.toString());
            if (StringUtils.isNotBlank(redirect)) {
                return Response.withRedirect(redirect).end();
            } else {
                return Response.withUnauthorized().andBody(Template.DEFAULT.forbidden()).end();
            }
        }

        return response;
    }
}