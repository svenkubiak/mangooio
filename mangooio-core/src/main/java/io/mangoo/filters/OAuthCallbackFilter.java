package io.mangoo.filters;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.google.inject.Inject;

import io.mangoo.authentication.Authentication;
import io.mangoo.configuration.Config;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.models.OAuthUser;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.RequestUtils;

/**
 *
 * @author svenkubiak
 *
 */
public class OAuthCallbackFilter implements MangooFilter {

    private Config config;

    @Inject
    public OAuthCallbackFilter(Config config) {
        this.config = config;
    }

    @Override
    public Response execute(Request request, Response response) {
        OAuthService oAuthService = RequestUtils.createOAuthService(request.getParameter("oauth"), this.config);
        if (oAuthService != null) {
            String oauthToken = request.getParameter("oauth_token");
            String oauthVerifier = request.getParameter("oauth_verifier");

            Token requestToken = new Token(oauthToken, oauthVerifier);
            Verifier verifier = new Verifier(oauthVerifier);

            Token accessToken = oAuthService.getAccessToken(requestToken, verifier);
            OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, "https://api.twitter.com/1.1/account/verify_credentials.json");
            oAuthService.signRequest(accessToken, oAuthRequest);

            org.scribe.model.Response scribeResponse = oAuthRequest.send();
            if (scribeResponse.isSuccessful()) {
                OAuthUser oAuthUser = new OAuthUser(scribeResponse.getBody());

                Authentication authentication = request.getAuthentication();
                authentication.setOAuthUser(oAuthUser);
                authentication.setAuthenticatedUser(oAuthUser.getUsername());
            }
        }

        return response;
    }
}