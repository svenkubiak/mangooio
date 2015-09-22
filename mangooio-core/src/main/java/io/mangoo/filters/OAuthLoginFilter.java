package io.mangoo.filters;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
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
        String oauth = request.getParameter("oauth");
        OAuthService oAuthService = RequestUtils.createOAuthService(request.getParameter("oauth"), this.config);
        if (oAuthService != null) {
            String url = null;
            if (("twitter").equals(oauth)) {
                Token requestToken = oAuthService.getRequestToken();
                url = oAuthService.getAuthorizationUrl(requestToken);
            } else if (("google").equals(oauth)) {
                url = oAuthService.getAuthorizationUrl(null);
            }

            if (StringUtils.isNotBlank(url)) {
                return Response.withRedirect(URI.create(url).toString()).end();
            }
        }

        return response;
    }
}