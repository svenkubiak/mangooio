package io.mangoo.filters;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Key;
import io.mangoo.enums.Required;
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
    private Config config;
    
    @Inject
    public AuthenticationFilter(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public Response execute(Request request, Response response) {
        if (!request.getAuthentication().isValid() || ( request.getAuthentication().isValid() && request.getAuthentication().isTwoFactor() )) {
            String redirect = this.config.getString(Key.AUTHENTICATION_REDIRECT.toString());
            if (StringUtils.isNotBlank(redirect)) {
                return Response.withRedirect(redirect).end();
            } else {
                return Response.withUnauthorized().andBody(Template.DEFAULT.forbidden()).end();
            }
        } 

        return response;
    }
}