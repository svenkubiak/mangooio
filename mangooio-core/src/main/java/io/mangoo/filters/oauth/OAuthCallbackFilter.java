package io.mangoo.filters.oauth;

import org.apache.commons.lang3.StringUtils;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.google.inject.Inject;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.mangoo.configuration.Config;
import io.mangoo.enums.oauth.OAuthProvider;
import io.mangoo.enums.oauth.OAuthResource;
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
    private static final String PROFILE_IMAGE_URL_HTTPS = "$.profile_image_url_https";
    private static final String SCREEN_NAME = "$.screen_name";
    private static final String PICTURE = "$.picture";
    private static final String PICTURE_DATA_URL = "$.picture.data.url";
    private static final String NAME = "$.name";
    private static final String ID = "$.id";
    private static final String OAUTH_VERIFIER = "oauth_verifier";
    private static final String OAUTH_TOKEN = "oauth_token";
    private static final String CODE = "code";
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

    /**
     * Executes an OAuth authentication to the Facebook API
     *
     * @param request The current request
     */
    private void facebookOAuth(Request request) {
        String code = request.getParameter(CODE);
        OAuthService oAuthService = RequestUtils.createOAuthService(OAuthProvider.FACEBOOK.toString(), this.config);

        if (StringUtils.isNotBlank(code) && oAuthService != null) {
            Verifier verifier = new Verifier(code);
            Token accessToken = oAuthService.getAccessToken(null, verifier);

            org.scribe.model.Response scribeResponse = getResourceResponse(oAuthService, accessToken, OAuthResource.FACEBOOK.toString());
            String scribeResponseBody = scribeResponse.getBody();
            if (scribeResponse.isSuccessful() && StringUtils.isNotBlank(scribeResponseBody)) {
                ReadContext readContext = JsonPath.parse(scribeResponseBody);
                request.getAuthentication().setOAuthUser(new OAuthUser(readContext.read(ID), scribeResponseBody, readContext.read(NAME), readContext.read(PICTURE_DATA_URL)));
            }
        }
    }

    /**
     * Executes an OAuth authentication to the Google API
     *
     * @param request The current request
     */
    private void googleOAuth(Request request) {
        String code = request.getParameter(CODE);
        OAuthService oAuthService = RequestUtils.createOAuthService(OAuthProvider.GOOGLE.toString(), this.config);

        if (StringUtils.isNotBlank(code) && oAuthService != null) {
            Verifier verifier = new Verifier(code);
            Token accessToken = oAuthService.getAccessToken(null, verifier);

            org.scribe.model.Response scribeResponse = getResourceResponse(oAuthService, accessToken, OAuthResource.GOOGLE.toString());
            String scribeResponseBody = scribeResponse.getBody();
            if (scribeResponse.isSuccessful() && StringUtils.isNotBlank(scribeResponseBody)) {
                ReadContext readContext = JsonPath.parse(scribeResponse.getBody());
                request.getAuthentication().setOAuthUser(new OAuthUser(readContext.read(ID), scribeResponseBody, readContext.read(NAME), readContext.read(PICTURE)));
            }
        }
    }

    /**
     * Executes an OAuth authentication to the Twitter API
     *
     * @param request The current request
     */
    private void twitterOAuth(Request request) {
        String oauthToken = request.getParameter(OAUTH_TOKEN);
        String oauthVerifier = request.getParameter(OAUTH_VERIFIER);
        OAuthService oAuthService = RequestUtils.createOAuthService(OAuthProvider.TWITTER.toString(), this.config);

        if (StringUtils.isNotBlank(oauthToken) && StringUtils.isNotBlank(oauthVerifier) && oAuthService != null) {
            Token requestToken = new Token(oauthToken, oauthVerifier);
            Verifier verifier = new Verifier(oauthVerifier);
            Token accessToken = oAuthService.getAccessToken(requestToken, verifier);

            org.scribe.model.Response scribeResponse = getResourceResponse(oAuthService, accessToken, OAuthResource.TWITTER.toString());
            String scribeResponseBody = scribeResponse.getBody();
            if (scribeResponse.isSuccessful() && StringUtils.isNotBlank(scribeResponseBody)) {
                ReadContext readContext = JsonPath.parse(scribeResponse.getBody());
                request.getAuthentication().setOAuthUser(new OAuthUser(readContext.read(ID), scribeResponse.getBody(), readContext.read(SCREEN_NAME), readContext.read(PROFILE_IMAGE_URL_HTTPS)));
            }
        }
    }

    /**
     * Executes a OAuth request to an OAuth resource
     *
     * @param oAuthService An OAuth service
     * @param accessToken An accessToken
     * @param resource The resource to access
     *
     * @return The response of the OAuth request
     */
    private org.scribe.model.Response getResourceResponse(OAuthService oAuthService, Token accessToken, String resource) {
        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, resource);
        oAuthService.signRequest(accessToken, oAuthRequest);

        return oAuthRequest.send();
    }
}