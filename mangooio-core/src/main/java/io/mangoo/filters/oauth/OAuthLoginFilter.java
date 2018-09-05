package io.mangoo.filters.oauth;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;
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
    private static final Logger LOG = LogManager.getLogger(OAuthLoginFilter.class);
    
    @Override
    public Response execute(Request request, Response response) {
        Optional<OAuthProvider> oAuthProvider = RequestUtils.getOAuthProvider(request.getParameter(Default.OAUTH_REQUEST_PARAMETER.toString()));
        if (oAuthProvider.isPresent()) {
            Optional<OAuthService> oAuthService = RequestUtils.createOAuthService(oAuthProvider.get());
            if (oAuthService.isPresent()) {
                String url = null;
                switch (oAuthProvider.get()) {
                    case TWITTER:
                        url = getTwitterUrl((OAuth10aService) oAuthService.get());
                        break;
                    case GOOGLE:
                    case FACEBOOK:
                        url = getOAuth2Url((OAuth20Service) oAuthService.get());
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

    private String getOAuth2Url(OAuth20Service oAuthService) {
        return oAuthService.getAuthorizationUrl();
    }

    private String getTwitterUrl(OAuth10aService oAuthService) {
        OAuth1RequestToken requestToken = null;
        try {
            requestToken = oAuthService.getRequestToken();
        } catch (IOException | InterruptedException | ExecutionException e) {
            LOG.error("Failed to get Url for twitter OAuth1", e);
            Thread.currentThread().interrupt();
        }
        return oAuthService.getAuthorizationUrl(requestToken);
    }
}