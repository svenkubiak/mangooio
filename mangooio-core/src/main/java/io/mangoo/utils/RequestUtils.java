package io.mangoo.utils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import com.google.common.net.MediaType;

import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
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
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.core.WebSocketChannel;

/**
 *
 * @author svenkubiak
 *
 */
public final class RequestUtils {
    private static final Logger LOG = LogManager.getLogger(RequestUtils.class);
    private static AttachmentKey<Attachment> attachmentKey;
    private static final String READ = "read";
    private static final String WRITE = "write";
    private static final Pattern PATTERN = Pattern.compile("\"");
    
    private RequestUtils() {
    }
    
    public static AttachmentKey<Attachment> getAttachmentKey() {
        if (attachmentKey == null) {
            attachmentKey = AttachmentKey.create(Attachment.class);
        }
        
        return attachmentKey;
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
        queryParameters.entrySet().forEach(entry -> requestParamater.put(entry.getKey(), entry.getValue().element()));

        return requestParamater;
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
        return headerMap != null && headerMap.get(Header.CONTENT_TYPE.toHttpString()) != null &&
                headerMap.get(Header.CONTENT_TYPE.toHttpString()).element().toLowerCase(Locale.ENGLISH).contains(MediaType.JSON_UTF_8.withoutParameters().toString());
    }

    /**
     * Checks if the given header contains a valid authentication
     *
     * @param cookie The cookie to parse
     * @return True if the cookie contains a valid authentication, false otherwise
     */
    public static boolean hasValidAuthentication(String cookie) {
        boolean valid = false;
        if (StringUtils.isNotBlank(cookie)) {
            Config config = Application.getInstance(Config.class);

            String value = null;
            String [] contents = cookie.split(";");
            for (String content : contents) {
                if (StringUtils.isNotBlank(content) && content.startsWith(config.getAuthenticationCookieName())) {
                    value = StringUtils.substringAfter(content, config.getAuthenticationCookieName() + "=");
                    value = PATTERN.matcher(value).replaceAll("");
                }
            }
            
            if (StringUtils.isNotBlank(value)) {
                String jwt = Application.getInstance(Crypto.class).decrypt(value, config.getAuthenticationCookieEncryptionKey());
                
                JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                        .setRequireExpirationTime()
                        .setRequireSubject()
                        .setVerificationKey(new HmacKey(config.getAuthenticationCookieSignKey().getBytes(StandardCharsets.UTF_8)))
                        .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
                        .build();
                try {
                    jwtConsumer.processToClaims(jwt);
                    valid = true;
                } catch (InvalidJwtException e) {
                    LOG.error("Failed to parse authentication cookie", e);
                }
            }
        }
        
        return valid;
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
    public static String getURL(URI uri) {
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
    public static HttpHandler wrapBasicAuthentication(HttpHandler httpHandler, String username, String password) {
        Objects.requireNonNull(httpHandler, Required.HTTP_HANDLER.toString());
        Objects.requireNonNull(username, Required.USERNAME.toString());
        Objects.requireNonNull(password, Required.PASSWORD.toString());
        
        HttpHandler wrap = new AuthenticationCallHandler(httpHandler);
        wrap = new AuthenticationConstraintHandler(wrap);
        wrap = new AuthenticationMechanismsHandler(wrap, Collections.<AuthenticationMechanism>singletonList(new BasicAuthenticationMechanism("Authentication required")));
        
        return new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, new Identity(username, password), wrap);
    }
    
    /**
     * Return if a given HTTP method results in a read or write request to a resource
     * 
     * GET = read
     * POST = write
     * PUT = write
     * DELETE = write
     * PATCH = write
     * OPTIONS = read
     * HEAD = read
     * 
     * @param method The HTTP method
     * @return read or write if HTTP method is found, blank otherwise
     */
    public static String getOperation(HttpString method) {
        String operation = "";
        
        if (Methods.POST.equals(method)) {
            operation = WRITE;
        } else if (Methods.PUT.equals(method)) {
            operation = WRITE;
        } else if (Methods.DELETE.equals(method)) {
            operation = WRITE;
        } else if (Methods.GET.equals(method)) {
            operation = READ;
        } else if (Methods.PATCH.equals(method)) {
            operation = WRITE;
        } else if (Methods.OPTIONS.equals(method)) {
            operation = READ;
        } else if (Methods.HEAD.equals(method)) {
            operation = READ;
        } else {
            // ignore everything else
        }
        
        return operation;
    }
}