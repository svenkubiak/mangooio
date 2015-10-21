package io.mangoo.utils;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.Preconditions;

import io.mangoo.authentication.oauth.Google2Api;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.ContentType;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.oauth.OAuthProvider;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

/**
 *
 * @author svenkubiak
 *
 */
public final class RequestUtils {
    private static final String SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    private RequestUtils() {
    }

    /**
     * Converts request and query parameter into a single map
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A single map contain both request and query parameter
     */
    public static Map<String, String> getRequestParameters(HttpServerExchange exchange) {
        Preconditions.checkNotNull(exchange, "HttpServerExchange can not be null");
        
        Map<String, String> requestParamater = new HashMap<String, String>();

        Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        queryParameters.putAll(exchange.getPathParameters());
        queryParameters.entrySet().forEach(entry -> requestParamater.put(entry.getKey(), entry.getValue().element())); //NOSONAR

        return requestParamater;
    }

    /**
     * Checks if a given template name has the current suffix and sets is
     * if it does not exist
     *
     * @param templateName The name of the template file
     * @return The template name with correct suffix
     */
    public static String getTemplateName(String templateName) {
        Preconditions.checkNotNull(templateName, "templateName can not be null");
        
        return templateName.endsWith(Default.TEMPLATE_SUFFIX.toString()) ? templateName : templateName + Default.TEMPLATE_SUFFIX.toString();
    }

    /**
     * Checks if the request is a POST or a PUT request
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request is a POST or a PUT request, false otherwise
     */
    public static boolean isPostOrPut(HttpServerExchange exchange) {
        Preconditions.checkNotNull(exchange, "HttpServerExchange can not be null");
        
        return (Methods.POST).equals(exchange.getRequestMethod()) || (Methods.PUT).equals(exchange.getRequestMethod());
    }

    /**
     * Checks if the requests content-type contains application/json
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request content-type contains application/json, false otherwise
     */
    public static boolean isJSONRequest(HttpServerExchange exchange) {
        Preconditions.checkNotNull(exchange, "HttpServerExchange can not be null");
        
        HeaderMap headerMap = exchange.getRequestHeaders();
        return headerMap != null && headerMap.get(Headers.CONTENT_TYPE) != null &&
                headerMap.get(Headers.CONTENT_TYPE).element().toLowerCase().contains(ContentType.APPLICATION_JSON.toString().toLowerCase());
    }

    /**
     * Creates an OAuthService for authentication a user with OAuth
     *
     * @param oauth The OAuth provider (twitter, google or facebook)
     * @param config The application configuration
     * @return An OAuthService object or null if creating failed
     */
    public static OAuthService createOAuthService(OAuthProvider oAuthProvider) {
        Preconditions.checkNotNull(oAuthProvider, "oAuthProvider can not be null");

        Config config = Application.getInstance(Config.class);
        ServiceBuilder serviceBuilder = null;
        switch (oAuthProvider) {
        case TWITTER:
            serviceBuilder = new ServiceBuilder()
            .provider(TwitterApi.class)
            .callback(config.getString(Key.OAUTH_TWITTER_CALLBACK))
            .apiKey(config.getString(Key.OAUTH_TWITTER_KEY))
            .apiSecret(config.getString(Key.OAUTH_TWITTER_SECRET));
            break;
        case GOOGLE:
            serviceBuilder = new ServiceBuilder()
            .provider(Google2Api.class)
            .scope(SCOPE)
            .callback(config.getString(Key.OAUTH_GOOGLE_CALLBACK))
            .apiKey(config.getString(Key.OAUTH_GOOGLE_KEY))
            .apiSecret(config.getString(Key.OAUTH_GOOGLE_SECRET));
            break;
        case FACEBOOK:
            serviceBuilder = new ServiceBuilder()
            .provider(FacebookApi.class)
            .callback(config.getString(Key.OAUTH_FACEBOOK_CALLBACK))
            .apiKey(config.getString(Key.OAUTH_FACEBOOK_KEY))
            .apiSecret(config.getString(Key.OAUTH_FACEBOOK_SECRET));
            break;            
        }

        return (serviceBuilder == null) ? null : serviceBuilder.build();
    }
    

    /**
     * Returns an OAuthProvider based on a given string
     * 
     * @param oauth The string to lookup the enum
     * @return OAuthProvider enum
     */
    public static OAuthProvider getOAuthProvider(String oauth) {
        OAuthProvider oAuthProvider = null;
        
        if (OAuthProvider.FACEBOOK.toString().equals(oauth)) {
            oAuthProvider = OAuthProvider.FACEBOOK;
        } else if (OAuthProvider.TWITTER.toString().equals(oauth)) {
            oAuthProvider = OAuthProvider.TWITTER;
        } else if (OAuthProvider.GOOGLE.toString().equals(oauth)) {
            oAuthProvider = OAuthProvider.GOOGLE;
        }   
        
        return oAuthProvider;
    }
}