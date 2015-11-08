package io.mangoo.filters.oauth;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import io.mangoo.enums.Default;
import io.mangoo.enums.oauth.OAuthProvider;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.RequestUtils;

/**
 * OAuth Login filter for redirecting to the OAuth provider
 *
 * @author svenkubiak
 *
 */
public class OAuthLoginFilter implements MangooFilter {
    @Override
    public Response execute(Request request, Response response) {
        OAuthProvider oAuthProvider = RequestUtils.getOAuthProvider(request.getParameter(Default.OAUTH_REQUEST_PARAMETER.toString()));
        if (oAuthProvider != null) {
            OAuthService oAuthService = RequestUtils.createOAuthService(oAuthProvider);
            if (oAuthService != null) {
                String url = null;
                switch (oAuthProvider) {
                case TWITTER:
                    Token requestToken = oAuthService.getRequestToken();
                    url = oAuthService.getAuthorizationUrl(requestToken);
                    break;
                case GOOGLE:
                case FACEBOOK:
                    url = oAuthService.getAuthorizationUrl(null);
                    break;
                default:
                break;
                }

                if (StringUtils.isNotBlank(url)) {
                    return Response.withRedirect(URI.create(url).toString()).end();
                }
            }
        }

        return response;
    }
}