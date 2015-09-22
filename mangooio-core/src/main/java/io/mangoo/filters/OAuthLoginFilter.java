package io.mangoo.filters;

import java.net.URI;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.RequestUtils;

/**
 *
 * @author svenkubiak
 *
 */
public class OAuthLoginFilter implements MangooFilter {

    private Config config;

    @Inject
    public OAuthLoginFilter(Config config) {
        this.config = config;
    }

    @Override
    public Response execute(Request request, Response response) {
        OAuthService oAuthService = RequestUtils.createOAuthService(request.getParameter("oauth"), this.config);
        if (oAuthService != null) {
            Token requestToken = oAuthService.getRequestToken();
            String url = oAuthService.getAuthorizationUrl(requestToken);

            return Response.withRedirect(URI.create(url).toString()).end();
        }

        return response;
    }
}