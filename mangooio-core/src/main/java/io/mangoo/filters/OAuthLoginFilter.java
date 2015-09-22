package io.mangoo.filters;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.enums.oauth.OAuthProvider;
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
    private static final String OAUTH = "oauth";
    private Config config;

    @Inject
    public OAuthLoginFilter(Config config) {
        this.config = config;
    }

    @Override
    public Response execute(Request request, Response response) {
        String oauth = request.getParameter(OAUTH);
        OAuthService oAuthService = RequestUtils.createOAuthService(oauth, this.config);
        if (oAuthService != null) {
            String url = null;
            if (OAuthProvider.TWITTER.toString().equals(oauth)) {
                Token requestToken = oAuthService.getRequestToken();
                url = oAuthService.getAuthorizationUrl(requestToken);
            } else if (OAuthProvider.GOOGLE.toString().equals(oauth) || OAuthProvider.FACEBOOK.toString().equals(oauth)) {
                url = oAuthService.getAuthorizationUrl(null);
            }

            if (StringUtils.isNotBlank(url)) {
                return Response.withRedirect(URI.create(url).toString()).end();
            }
        }

        return response;
    }
}