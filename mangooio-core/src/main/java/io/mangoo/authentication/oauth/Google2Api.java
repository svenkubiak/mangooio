package io.mangoo.authentication.oauth;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.*;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Google OAuth2.0
 * Released under the same license as scribe (MIT License)
 *
 * @author yincrash
 *
 */
public class Google2Api extends DefaultApi20 {
    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=%s&redirect_uri=%s";
    private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s";
    private static final String ACCESS_TOKEN_ENDPOINT = "https://accounts.google.com/o/oauth2/token";
    private static final Pattern PATTERN = Pattern.compile("\"access_token\" : \"([^&\"]+)\"");

    @Override
    public String getAccessTokenEndpoint() {
        return ACCESS_TOKEN_ENDPOINT;
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new GoogleApi2AccessTokenExtractor();
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        Objects.requireNonNull(config, "config can not be null");

        if (config.hasScope()) {
            return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(),
                    OAuthEncoder.encode(config.getCallback()),
                    OAuthEncoder.encode(config.getScope()));
        } else {
            return String.format(AUTHORIZE_URL, config.getApiKey(),
                    OAuthEncoder.encode(config.getCallback()));
        }
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public OAuthService createService(OAuthConfig config) {
        Objects.requireNonNull(config, "config can not be null");

        return new GoogleOAuth2Service(this, config);
    }

    private static class GoogleApi2AccessTokenExtractor implements AccessTokenExtractor {
        @Override
        public Token extract(String response) {
            Preconditions.checkEmptyString(response, "Response body is incorrect. Can't extract a token from an empty string");

            Matcher matcher = PATTERN.matcher(response);
            if (matcher.find()) {
                String token = OAuthEncoder.decode(matcher.group(1));
                return new Token(token, "", response);
            } else {
                throw new OAuthException("Response body is incorrect. Can't extract a token from this: '" + response + "'", null);
            }
        }
    }

    private class GoogleOAuth2Service extends OAuth20ServiceImpl {
        private static final String AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
        private final DefaultApi20 api;
        private final OAuthConfig config;

        public GoogleOAuth2Service(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }

        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
            switch (api.getAccessTokenVerb()) {
            case POST:
                request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
                request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                request.addBodyParameter(GRANT_TYPE, AUTHORIZATION_CODE);
                break;
            case GET:
            default:
                request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
                request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                if (config.hasScope()) {
                    request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());
                }
            }
            Response response = request.send();

            return api.getAccessTokenExtractor().extract(response.getBody());
        }
    }
}