package io.mangoo.utils;

import com.google.common.net.MediaType;
import com.google.re2j.Pattern;
import io.mangoo.constants.Header;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.jwt.JwtParser;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Methods;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public final class RequestUtils {
    private static final Logger LOG = LogManager.getLogger(RequestUtils.class);
    private static final Pattern PATTERN = Pattern.compile("\"");
    private static AttachmentKey<Attachment> attachmentKey;

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
        Objects.requireNonNull(exchange, NotNull.HTTP_SERVER_EXCHANGE);

        final Map<String, String> requestParameter = new HashMap<>();
        final Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        queryParameters.putAll(exchange.getPathParameters());
        queryParameters.forEach((key, value) -> requestParameter.put(key, value.element()));

        return requestParameter;
    }

    /**
     * Checks if the request is a POST, PUT or PATCH request
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request is a POST, PUT or PATCH request, false otherwise
     */
    public static boolean isPostPutPatch(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, NotNull.HTTP_SERVER_EXCHANGE);

        return (Methods.POST).equals(exchange.getRequestMethod()) || (Methods.PUT).equals(exchange.getRequestMethod()) || (Methods.PATCH).equals(exchange.getRequestMethod());
    }
    
    /**
     * Checks if the requests content-type contains application/json
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request content-type contains application/json, false otherwise
     */
    public static boolean isJsonRequest(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, NotNull.HTTP_SERVER_EXCHANGE);

        var headerMap = exchange.getRequestHeaders();
        return headerMap != null && headerMap.get(Header.CONTENT_TYPE) != null &&
               headerMap.get(Header.CONTENT_TYPE).element().toLowerCase(Locale.ENGLISH).contains(MediaType.JSON_UTF_8.withoutParameters().toString());
    }

    /**
     * Checks if the given header contains a valid authentication
     *
     * @param cookie The cookie to parse
     * @return True if the cookie contains a valid authentication, false otherwise
     */
    public static boolean hasValidAuthentication(String cookie) {
        var valid = false;
        if (StringUtils.isNotBlank(cookie)) {
            var config = Application.getInstance(Config.class);

            String value = null;
            String [] contents = cookie.split(";");
            for (String content : contents) {
                if (StringUtils.isNotBlank(content) && content.startsWith(config.getAuthenticationCookieName())) {
                    value = StringUtils.substringAfter(content, config.getAuthenticationCookieName() + "=");
                    value = PATTERN.matcher(value).replaceAll("");
                }
            }
            
            if (StringUtils.isNotBlank(value)) {
                try {
                    JwtParser.create()
                        .withSharedSecret(config.getAuthenticationCookieSecret())
                        .withCookieValue(value)
                        .parse();
                    
                    valid = true;
                } catch (MangooTokenException e) {
                    LOG.error("Failed to parse authentication cookie", e);
                }
            }
        }
        
        return valid;
    }
}