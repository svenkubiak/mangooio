package io.mangoo.filters.oauth;

import java.net.URI;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuthService;

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
        Optional<OAuthProvider> oAuthProvider = RequestUtils.getOAuthProvider(request.getParameter(Default.OAUTH_REQUEST_PARAMETER.toString()));
        if (oAuthProvider.isPresent()) {
            Optional<OAuthService> oAuthService = RequestUtils.createOAuthService(oAuthProvider.get());
            if (oAuthService.isPresent()) {
                String url = null;
                switch (oAuthProvider.get()) {
                case TWITTER:
                    OAuth10aService twitterService = (OAuth10aService) oAuthService.get();
                    OAuth1RequestToken requestToken = twitterService.getRequestToken();
                    url = twitterService.getAuthorizationUrl(requestToken);
                    break;
                case GOOGLE:
                    OAuth10aService googleService = (OAuth10aService) oAuthService.get();
                    url = googleService.getAuthorizationUrl(null);
                    break;
                case FACEBOOK:
                    OAuth10aService facebookService = (OAuth10aService) oAuthService.get();
                    url = facebookService.getAuthorizationUrl(null);
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