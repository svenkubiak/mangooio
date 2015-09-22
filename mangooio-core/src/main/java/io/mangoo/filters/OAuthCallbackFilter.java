package io.mangoo.filters;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.boon.json.JsonFactory;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.google.inject.Inject;

import io.mangoo.authentication.Authentication;
import io.mangoo.configuration.Config;
import io.mangoo.enums.OAuthProvider;
import io.mangoo.enums.OAuthResource;
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
        String oauth = request.getParameter("oauth");
        if (OAuthProvider.TWITTER.toString().equals(oauth)) {
            twitterOAuth(request);
        } else if (OAuthProvider.GOOGLE.toString().equals(oauth)) {
            googleOAuth(request);
        } else if (OAuthProvider.FACEBOOK.toString().equals(oauth)) {
            facebookOAuth(request);
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    private void facebookOAuth(Request request) {
        String code = request.getParameter("code");
        OAuthService oAuthService = RequestUtils.createOAuthService(OAuthProvider.FACEBOOK.toString(), this.config);

        if (StringUtils.isNotBlank(code) && oAuthService != null) {
            Verifier verifier = new Verifier(code);
            Token accessToken = oAuthService.getAccessToken(null, verifier);

            org.scribe.model.Response scribeResponse = getResourceResponse(oAuthService, accessToken, OAuthResource.FACEBOOK.toString());
            if (scribeResponse.isSuccessful()) {
                Map<String, Object> json = JsonFactory.create().readValue(scribeResponse.getBody(), Map.class);
                OAuthUser oAuthUser = new OAuthUser(scribeResponse.getBody(), (String) json.get("name"), (String) json.get("picture"));

                Authentication authentication = request.getAuthentication();
                authentication.setOAuthUser(oAuthUser);
                authentication.setAuthenticatedUser(oAuthUser.getUsername());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void googleOAuth(Request request) {
        String code = request.getParameter("code");
        OAuthService oAuthService = RequestUtils.createOAuthService(OAuthProvider.GOOGLE.toString(), this.config);

        if (StringUtils.isNotBlank(code) && oAuthService != null) {
            Verifier verifier = new Verifier(code);
            Token accessToken = oAuthService.getAccessToken(null, verifier);

            org.scribe.model.Response scribeResponse = getResourceResponse(oAuthService, accessToken, OAuthResource.GOOGLE.toString());
            if (scribeResponse.isSuccessful()) {
                Map<String, Object> json = JsonFactory.create().readValue(scribeResponse.getBody(), Map.class);
                OAuthUser oAuthUser = new OAuthUser(scribeResponse.getBody(), (String) json.get("name"), (String) json.get("picture"));

                Authentication authentication = request.getAuthentication();
                authentication.setOAuthUser(oAuthUser);
                authentication.setAuthenticatedUser(oAuthUser.getUsername());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void twitterOAuth(Request request) {
        String oauthToken = request.getParameter("oauth_token");
        String oauthVerifier = request.getParameter("oauth_verifier");
        OAuthService oAuthService = RequestUtils.createOAuthService(OAuthProvider.TWITTER.toString(), this.config);

        if (StringUtils.isNotBlank(oauthToken) && StringUtils.isNotBlank(oauthVerifier) && oAuthService != null) {
            Token requestToken = new Token(oauthToken, oauthVerifier);
            Verifier verifier = new Verifier(oauthVerifier);
            Token accessToken = oAuthService.getAccessToken(requestToken, verifier);

            org.scribe.model.Response scribeResponse = getResourceResponse(oAuthService, accessToken, OAuthResource.TWITTER.toString());
            if (scribeResponse.isSuccessful()) {
                Map<String, Object> json = JsonFactory.create().readValue(scribeResponse.getBody(), Map.class);
                OAuthUser oAuthUser = new OAuthUser(scribeResponse.getBody(), (String) json.get("screen_name"), (String) json.get("profile_image_url_https"));

                Authentication authentication = request.getAuthentication();
                authentication.setOAuthUser(oAuthUser);
                authentication.setAuthenticatedUser(oAuthUser.getUsername());
            }
        }
    }

    private org.scribe.model.Response getResourceResponse(OAuthService oAuthService, Token accessToken, String resource) {
        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, resource);
        oAuthService.signRequest(accessToken, oAuthRequest);

        return oAuthRequest.send();
    }
}