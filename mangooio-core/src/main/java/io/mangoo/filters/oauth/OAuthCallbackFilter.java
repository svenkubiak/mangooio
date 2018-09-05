package io.mangoo.filters.oauth;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
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
    private static final Logger LOG = LogManager.getLogger(OAuthCallbackFilter.class);
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
        final Optional<OAuthProvider> oAuthProvider = RequestUtils.getOAuthProvider(request.getParameter(Default.OAUTH_REQUEST_PARAMETER.toString()));
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
        final String code = request.getParameter(CODE);
        final Optional<OAuthService> oAuthService = RequestUtils.createOAuthService(OAuthProvider.FACEBOOK);

        if (StringUtils.isNotBlank(code) && oAuthService.isPresent()) {
            final OAuth20Service oAuth20Service = (OAuth20Service ) oAuthService.get();
            OAuth2AccessToken oAuth2AccessToken = null;
            try {
                oAuth2AccessToken = oAuth20Service.getAccessToken(code);
            } catch (IOException | InterruptedException | ExecutionException e) {
                LOG.error("Failed to get facebook OAuth2 accesstoken", e);
                Thread.currentThread().interrupt();
            }

            if (oAuth2AccessToken != null) {
                com.github.scribejava.core.model.Response scribeResponse = null;
                String scribeResponseBody = null;
                try {
                    scribeResponse = getResourceResponse(oAuth20Service, oAuth2AccessToken, OAuthResource.FACEBOOK.toString());
                    scribeResponseBody = scribeResponse.getBody();
                } catch (IOException | InterruptedException | ExecutionException e) {
                    LOG.error("Failed to get response body for facebook OAuth2", e);
                    Thread.currentThread().interrupt();
                }
                
                if (scribeResponse != null && scribeResponse.isSuccessful() && StringUtils.isNotBlank(scribeResponseBody)) {
                    final ReadContext readContext = JsonPath.parse(scribeResponseBody);
                    request.getAuthentication().withOAuthUser(new OAuthUser(readContext.read(ID), scribeResponseBody, readContext.read(NAME), readContext.read(PICTURE_DATA_URL)));
                }                
            }
        }
    }

    /**
     * Executes an OAuth authentication to the Google API
     *
     * @param request The current request
     */
    private void googleOAuth(Request request) {
        final String code = request.getParameter(CODE);
        final Optional<OAuthService> oAuthService = RequestUtils.createOAuthService(OAuthProvider.GOOGLE);

        if (StringUtils.isNotBlank(code) && oAuthService.isPresent()) {
            final OAuth20Service oAuth20Service = (OAuth20Service) oAuthService.get();
            OAuth2AccessToken oAuth2AccessToken = null;
            try {
                oAuth2AccessToken = oAuth20Service.getAccessToken(code);
            } catch (IOException | InterruptedException | ExecutionException e) {
                LOG.error("Failed to get google OAuth2 access token", e);
                Thread.currentThread().interrupt();
            }

            if (oAuth2AccessToken != null) {
                com.github.scribejava.core.model.Response scribeResponse = null;
                String scribeResponseBody = null;
                try {
                    scribeResponse = getResourceResponse(oAuth20Service, oAuth2AccessToken, OAuthResource.GOOGLE.toString());
                    scribeResponseBody = scribeResponse.getBody();
                } catch (IOException | InterruptedException | ExecutionException e) {
                    LOG.error("Failed to get response body for goolge OAuth2", e);
                    Thread.currentThread().interrupt();
                }
                
                if (scribeResponse != null && scribeResponse.isSuccessful() && StringUtils.isNotBlank(scribeResponseBody)) {
                    final ReadContext readContext = JsonPath.parse(scribeResponseBody);
                    request.getAuthentication().withOAuthUser(new OAuthUser(readContext.read(ID), scribeResponseBody, readContext.read(NAME), readContext.read(PICTURE)));
                }  
            }
        }
    }

    /**
     * Executes an OAuth authentication to the Twitter API
     *
     * @param request The current request
     */
    private void twitterOAuth(Request request) {
        final String oauthToken = request.getParameter(OAUTH_TOKEN);
        final String oauthVerifier = request.getParameter(OAUTH_VERIFIER);
        final Optional<OAuthService> oAuthService = RequestUtils.createOAuthService(OAuthProvider.TWITTER);

        if (StringUtils.isNotBlank(oauthToken) && StringUtils.isNotBlank(oauthVerifier) && oAuthService.isPresent()) {
            final OAuth1RequestToken requestToken = new OAuth1RequestToken(oauthToken, oauthVerifier);
            final OAuth10aService oAuth10aService = (OAuth10aService) oAuthService.get();
            OAuth1AccessToken oAuth1AccessToken = null;
            try {
                oAuth1AccessToken = oAuth10aService.getAccessToken(requestToken, oauthVerifier);
            } catch (IOException | InterruptedException | ExecutionException e) {
                LOG.error("Failed to get twitter OAuth access token", e);
                Thread.currentThread().interrupt();
            }

            if (oAuth1AccessToken != null) {
                com.github.scribejava.core.model.Response scribeResponse = null;
                String scribeResponseBody = null;
                try {
                    scribeResponse = getResourceResponse(oAuth10aService, oAuth1AccessToken, OAuthResource.TWITTER.toString());
                    scribeResponseBody = scribeResponse.getBody();
                } catch (IOException | InterruptedException | ExecutionException e) {
                    LOG.error("Failed to get response body for goolge OAuth1" ,e);
                    Thread.currentThread().interrupt();
                }
                
                if (scribeResponse != null && scribeResponse.isSuccessful() && StringUtils.isNotBlank(scribeResponseBody)) {
                    final ReadContext readContext = JsonPath.parse(scribeResponseBody);
                    request.getAuthentication().withOAuthUser(new OAuthUser(readContext.read(ID), scribeResponseBody, readContext.read(SCREEN_NAME), readContext.read(PROFILE_IMAGE_URL_HTTPS)));
                }  
            }
        }
    }

    /**
     * Executes an OAuth20 request
     *
     * @param oAuth20Service The service to access
     * @param oAuth2AccessToken The access token to be used
     * @param resource The resource URL to access
     *
     * @return A OAuth response
     * @throws IOException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    private com.github.scribejava.core.model.Response getResourceResponse(OAuth20Service oAuth20Service, OAuth2AccessToken oAuth2AccessToken, String resource) throws InterruptedException, ExecutionException, IOException {
        final OAuthRequest request = new OAuthRequest(Verb.GET, resource);
        oAuth20Service.signRequest(oAuth2AccessToken, request);

        return oAuth20Service.execute(request);
    }

    /**
     * Executes an OAuth10a request
     *
     * @param oAuth10aService The service to access
     * @param oAuth1AccessToken The access token to be used
     * @param resource The resource URL to access
     *
     * @return A OAuth response
     * @throws IOException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    private com.github.scribejava.core.model.Response getResourceResponse(OAuth10aService oAuth10aService, OAuth1AccessToken oAuth1AccessToken, String resource) throws InterruptedException, ExecutionException, IOException {
        final OAuthRequest request = new OAuthRequest(Verb.GET, resource);
        oAuth10aService.signRequest(oAuth1AccessToken, request);

        return oAuth10aService.execute(request);
    }
}