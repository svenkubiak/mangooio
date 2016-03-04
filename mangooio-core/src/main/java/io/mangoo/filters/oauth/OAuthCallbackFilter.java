package io.mangoo.filters.oauth;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.model.Verifier;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.mangoo.enums.Default;
import io.mangoo.enums.oauth.OAuthProvider;
import io.mangoo.enums.oauth.OAuthResource;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.models.OAuthUser;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.RequestUtils;

/**
 * Callback filter when returning from an OAuth authentication
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

    @Override
    public Response execute(Request request, Response response) {
        Optional<OAuthProvider> oAuthProvider = RequestUtils.getOAuthProvider(request.getParameter(Default.OAUTH_REQUEST_PARAMETER.toString()));
        if (oAuthProvider.isPresent()) {
            switch (oAuthProvider.get()) {
            case TWITTER:
                twitterOAuth(request);
                break;
            case GOOGLE:
                googleOAuth(request);
                break;
            case FACEBOOK:
                facebookOAuth(request);
                break;
            default:
                break;
            } 
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
        Optional<OAuthService> oAuthService = RequestUtils.createOAuthService(OAuthProvider.FACEBOOK);

        if (StringUtils.isNotBlank(code) && oAuthService.isPresent()) {
            Verifier verifier = new Verifier(code);
            OAuth10aService oAuth10aService = (OAuth10aService) oAuthService.get();
            Token accessToken = oAuth10aService.getAccessToken(null, verifier);

            com.github.scribejava.core.model.Response scribeResponse = getResourceResponse(oAuth10aService, accessToken, OAuthResource.FACEBOOK.toString());
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
//        String code = request.getParameter(CODE);
//        Optional<OAuthService> oAuthService = RequestUtils.createOAuthService(OAuthProvider.GOOGLE);
//
//        if (StringUtils.isNotBlank(code) && oAuthService.isPresent()) {
//            Verifier verifier = new Verifier(code);
//            OAuth20Service oAuth20Service = (OAuth20Service) oAuthService.get();
//            Token accessToken = oAuth20Service.getAccessToken(verifier);
//
//            com.github.scribejava.core.model.Response scribeResponse = getResourceResponse(oAuth20Service, accessToken, OAuthResource.GOOGLE.toString());
//            String scribeResponseBody = scribeResponse.getBody();
//            if (scribeResponse.isSuccessful() && StringUtils.isNotBlank(scribeResponseBody)) {
//                ReadContext readContext = JsonPath.parse(scribeResponse.getBody());
//                request.getAuthentication().setOAuthUser(new OAuthUser(readContext.read(ID), scribeResponseBody, readContext.read(NAME), readContext.read(PICTURE)));
//            }
//        }
    }

    /**
     * Executes an OAuth authentication to the Twitter API
     *
     * @param request The current request
     */
    private void twitterOAuth(Request request) {
        String oauthToken = request.getParameter(OAUTH_TOKEN);
        String oauthVerifier = request.getParameter(OAUTH_VERIFIER);
        Optional<OAuthService> oAuthService = RequestUtils.createOAuthService(OAuthProvider.TWITTER);

        if (StringUtils.isNotBlank(oauthToken) && StringUtils.isNotBlank(oauthVerifier) && oAuthService.isPresent()) {
            OAuth1RequestToken requestToken = new OAuth1RequestToken(oauthToken, oauthVerifier);
            Verifier verifier = new Verifier(oauthVerifier);
            OAuth10aService oAuth10aService = (OAuth10aService) oAuthService.get();
            Token accessToken = oAuth10aService.getAccessToken(requestToken, verifier);

            com.github.scribejava.core.model.Response scribeResponse = getResourceResponse(oAuth10aService, accessToken, OAuthResource.TWITTER.toString());
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
    private com.github.scribejava.core.model.Response getResourceResponse(OAuth10aService oAuth10aService, Token accessToken, String resource) {
//        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, resource, oAuth10aService);
//        oAuth10aService.signRequest(accessToken, oAuthRequest);
//
//        return oAuthRequest.send();
        return null;
    }
}