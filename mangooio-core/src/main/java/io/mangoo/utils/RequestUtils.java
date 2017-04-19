package io.mangoo.utils;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuthService;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.ContentType;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Required;
import io.mangoo.enums.oauth.OAuthProvider;
import io.mangoo.models.Identity;
import io.mangoo.routing.Attachment;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Cookies;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.websockets.core.WebSocketChannel;

/**
 *
 * @author svenkubiak
 *
 */
public final class RequestUtils {
    public static final AttachmentKey<Attachment> ATTACHMENT_KEY = AttachmentKey.create(Attachment.class);
    private static final String SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static final int MAX_RANDOM = 999_999;
    private static final int AUTH_PREFIX_LENGTH = 3;
    private static final int INDEX_0 = 0;
    private static final int INDEX_1 = 1;
    private static final int INDEX_2 = 2;

    private RequestUtils() {
    }

    /**
     * Converts request and query parameter into a single map
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A single map contain both request and query parameter
     */
    public static Map<String, String> getRequestParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE.toString());

        final Map<String, String> requestParamater = new HashMap<>();
        final Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        queryParameters.putAll(exchange.getPathParameters());
        queryParameters.entrySet().forEach(entry -> requestParamater.put(entry.getKey(), entry.getValue().element())); //NOSONAR

        return requestParamater;
    }

    /**
     * Checks if the request is a POST or a PUT request
     *
     * @deprecated As of version 4.4.0, replaced by {@link #isPostPutPatch(HttpServerExchange)}
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request is a POST or a PUT request, false otherwise
     */
    @Deprecated
    public static boolean isPostOrPut(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE.toString());

        return (Methods.POST).equals(exchange.getRequestMethod()) || (Methods.PUT).equals(exchange.getRequestMethod());
    }

    /**
     * Checks if the request is a POST, PUT or PATCH request
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request is a POST, PUT or PATCH request, false otherwise
     */
    public static boolean isPostPutPatch(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE.toString());

        return (Methods.POST).equals(exchange.getRequestMethod()) || (Methods.PUT).equals(exchange.getRequestMethod()) || (Methods.PATCH).equals(exchange.getRequestMethod());
    }
    
    /**
     * Checks if the requests content-type contains application/json
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request content-type contains application/json, false otherwise
     */
    public static boolean isJsonRequest(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE.toString());

        final HeaderMap headerMap = exchange.getRequestHeaders();
        return headerMap != null && headerMap.get(Headers.CONTENT_TYPE) != null &&
                headerMap.get(Headers.CONTENT_TYPE).element().toLowerCase(Locale.ENGLISH).contains(ContentType.APPLICATION_JSON.toString().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Creates an OAuthService for authentication a user with OAuth
     *
     * @param oAuthProvider The OAuth provider Enum
     * @return An OAuthService object or null if creating failed
     */
    @SuppressWarnings("rawtypes")
    public static Optional<OAuthService> createOAuthService(OAuthProvider oAuthProvider) {
        Objects.requireNonNull(oAuthProvider, Required.OAUTH_PROVIDER.toString());

        Config config = Application.getInstance(Config.class);
        OAuthService oAuthService = null;
        switch (oAuthProvider) {
        case TWITTER:
            oAuthService = new ServiceBuilder()
            .callback(config.getString(Key.OAUTH_TWITTER_CALLBACK))
            .apiKey(config.getString(Key.OAUTH_TWITTER_KEY))
            .apiSecret(config.getString(Key.OAUTH_TWITTER_SECRET))
            .build(TwitterApi.instance());
            break;
        case GOOGLE:
            oAuthService = new ServiceBuilder()
            .scope(SCOPE)
            .callback(config.getString(Key.OAUTH_GOOGLE_CALLBACK))
            .apiKey(config.getString(Key.OAUTH_GOOGLE_KEY))
            .apiSecret(config.getString(Key.OAUTH_GOOGLE_SECRET))
            .state("secret" + new SecureRandom().nextInt(MAX_RANDOM))
            .build(GoogleApi20.instance());
            break;
        case FACEBOOK:
            oAuthService = new ServiceBuilder()
            .callback(config.getString(Key.OAUTH_FACEBOOK_CALLBACK))
            .apiKey(config.getString(Key.OAUTH_FACEBOOK_KEY))
            .apiSecret(config.getString(Key.OAUTH_FACEBOOK_SECRET))
            .build(FacebookApi.instance());
            break;
        default:
            break;
        }

        return (oAuthService == null) ? Optional.empty() : Optional.of(oAuthService);
    }


    /**
     * Returns an OAuthProvider based on a given string
     *
     * @param oauth The string to lookup the OAuthProvider Enum
     * @return OAuthProvider Enum
     */
    public static Optional<OAuthProvider> getOAuthProvider(String oauth) {
        OAuthProvider oAuthProvider = null;
        if (OAuthProvider.FACEBOOK.toString().equals(oauth)) {
            oAuthProvider = OAuthProvider.FACEBOOK;
        } else if (OAuthProvider.TWITTER.toString().equals(oauth)) {
            oAuthProvider = OAuthProvider.TWITTER;
        } else if (OAuthProvider.GOOGLE.toString().equals(oauth)) {
            oAuthProvider = OAuthProvider.GOOGLE;
        }

        return (oAuthProvider == null) ? Optional.empty() : Optional.of(oAuthProvider);
    }

    /**
     * Checks if the given header contains a valid authentication
     *
     * @param cookieHeader The header to parse
     * @return True if the cookie contains a valid authentication, false otherwise
     */
    public static boolean hasValidAuthentication(String cookieHeader) {
        boolean validAuthentication = false;
        if (StringUtils.isNotBlank(cookieHeader)) {
            final Map<String, Cookie> cookies = Cookies.parseRequestCookies(1, false, Arrays.asList(cookieHeader));

            Config config = Application.getInstance(Config.class);
            String cookieValue = cookies.get(config.getAuthenticationCookieName()).getValue();
            if (StringUtils.isNotBlank(cookieValue) && !("null").equals(cookieValue)) {
                if (config.isAuthenticationCookieEncrypt()) {
                    cookieValue = Application.getInstance(Crypto.class).decrypt(cookieValue);
                }

                String sign = null;
                String expires = null;
                String version = null;
                final String prefix = StringUtils.substringBefore(cookieValue, Default.DATA_DELIMITER.toString());

                if (StringUtils.isNotBlank(prefix)) {
                    final String [] prefixes = prefix.split("\\" + Default.DELIMITER.toString());
                    if (prefixes != null && prefixes.length == AUTH_PREFIX_LENGTH) {
                        sign = prefixes [INDEX_0];
                        expires = prefixes [INDEX_1];
                        version = prefixes [INDEX_2];
                    }
                }

                if (StringUtils.isNotBlank(sign) && StringUtils.isNotBlank(expires)) {
                    final String authenticatedUser = cookieValue.substring(cookieValue.indexOf(Default.DATA_DELIMITER.toString()) + 1, cookieValue.length());
                    final LocalDateTime expiresDate = LocalDateTime.parse(expires);

                    if (LocalDateTime.now().isBefore(expiresDate) && DigestUtils.sha512Hex(authenticatedUser + expires + version + config.getApplicationSecret()).equals(sign)) {
                        validAuthentication = true;
                    }
                }
            }
        }

        return validAuthentication;
    }

    /**
     * Retrieves a URL from a Server-Sent Event connection
     *
     * @param connection The ServerSentEvent Connection
     *
     * @return The URL of the Server-Sent Event Connection
     */
    public static String getServerSentEventURL(ServerSentEventConnection connection) {
        return getURL(URI.create(connection.getRequestURI()));
    }

    /**
     * Retrieves the URL of a WebSocketChannel
     *
     * @param channel The WebSocket Channel
     *
     * @return The URL of the WebSocket Channel
     */
    public static String getWebSocketURL(WebSocketChannel channel) {
        return getURL(URI.create(channel.getUrl()));
    }

    /**
     * Creates and URL with only path and if present query and
     * fragment, e.g. /path/data?key=value#fragid1
     *
     * @param uri The URI to generate from
     * @return The generated URL
     */
    private static String getURL(URI uri) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(uri.getPath());
        
        String query = uri.getQuery();
        String fragment = uri.getFragment();

        if (StringUtils.isNotBlank(query)) {
            buffer.append('?').append(query);
        }

        if (StringUtils.isNotBlank(fragment)) {
            buffer.append('#').append(fragment);
        }

        return buffer.toString();
    }
    
    /**
     * Adds a Wrapper to the handler when the request requires authentication
     * 
     * @param httpHandler The Handler to wrap
     * @param username The username to use
     * @param password The password to use
     * @return An HttpHandler wrapped through BasicAuthentication
     */
    public static HttpHandler wrapSecurity(HttpHandler httpHandler, String username, String password) {
        Objects.requireNonNull(httpHandler, Required.HTTP_HANDLER.toString());
        Objects.requireNonNull(username, Required.USERNAME.toString());
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        
        HttpHandler wrap = new AuthenticationCallHandler(httpHandler);
        wrap = new AuthenticationConstraintHandler(wrap);
        wrap = new AuthenticationMechanismsHandler(wrap, Collections.<AuthenticationMechanism>singletonList(new BasicAuthenticationMechanism("Authentication required")));
        
        return new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, new Identity(username, password), wrap);
    }
}