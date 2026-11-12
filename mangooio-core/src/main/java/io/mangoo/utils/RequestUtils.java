package io.mangoo.utils;

import io.mangoo.constants.Default;
import io.mangoo.constants.Header;
import io.mangoo.constants.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Request;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Methods;
import io.undertow.util.PathTemplateMatch;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class RequestUtils {
    private static final AttachmentKey<Attachment> ATTACHMENT_KEY = AttachmentKey.create(Attachment.class);

    private RequestUtils() {
    }

    public static AttachmentKey<Attachment> getAttachmentKey() {
        return ATTACHMENT_KEY;
    }
    
    /**
     * Route parameters are resolved by the router and cannot be forged by a client, so only they may be used for authorization decisions.
     */
    public static Map<String, String> getPathParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        final Map<String, String> pathParameter = new HashMap<>();

        exchange.getPathParameters().forEach((key, value) -> pathParameter.put(key, value.element()));

        // The router's PathTemplateMatch is authoritative and therefore applied last
        var pathTemplateMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        if (pathTemplateMatch != null) {
            pathParameter.putAll(pathTemplateMatch.getParameters());
        }

        return pathParameter;
    }

    /**
     * Query parameters are always untrusted client input.
     */
    public static Map<String, String> getQueryParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        final Map<String, String> queryParameter = new HashMap<>();
        exchange.getQueryParameters().forEach((key, value) -> queryParameter.put(key, value.element()));

        return queryParameter;
    }

    /**
     * A route parameter always wins over a query parameter of the same name.
     */
    public static Map<String, String> getRequestParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        final Map<String, String> requestParameter = getQueryParameters(exchange);
        requestParameter.putAll(getPathParameters(exchange));

        return requestParameter;
    }

    /**
     * Returns true if a query parameter has the same name as a route parameter, e.g. /foo/abc?id=xyz on /foo/{id}.
     * The route value wins, but an application may want to reject such a request.
     */
    public static boolean hasAmbiguousParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        Map<String, String> pathParameter = getPathParameters(exchange);
        if (pathParameter.isEmpty()) {
            return false;
        }

        return pathParameter.keySet().stream().anyMatch(exchange.getQueryParameters()::containsKey);
    }

    /** True if a query parameter key appears more than once after URL decoding or cannot be decoded (HTTP parameter pollution). */
    public static boolean hasMultipleParameterValues(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        var queryString = exchange.getQueryString();
        if (StringUtils.isBlank(queryString)) {
            return false;
        }

        Set<String> seenKeys = new HashSet<>();
        for (String pair : queryString.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }

            int index = pair.indexOf('=');
            String key = index >= 0 ? pair.substring(0, index) : pair;
            try {
                key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                return true;
            }

            if (!seenKeys.add(key)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPostPutPatch(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        return (Methods.POST).equals(exchange.getRequestMethod()) || (Methods.PUT).equals(exchange.getRequestMethod()) || (Methods.PATCH).equals(exchange.getRequestMethod());
    }
    
    public static boolean isJsonRequest(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        String contentType = contentType(exchange);
        return contentType != null && (contentType.startsWith("application/json") || contentType.contains("+json"));
    }

    public static boolean isFormRequest(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        String contentType = contentType(exchange);
        return contentType != null && (contentType.startsWith("multipart/form-data") || contentType.startsWith("application/x-www-form-urlencoded"));
    }

    /** JSON whose Content-Length is known and fits in one 16 KiB buffer. */
    public static boolean isSmallJsonRequest(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        long contentLength = exchange.getRequestContentLength();
        return isJsonRequest(exchange) && contentLength >= 0 && contentLength <= Default.SMALL_JSON_MAX_BYTES;
    }

    private static String contentType(HttpServerExchange exchange) {
        String contentType = exchange.getRequestHeaders().getFirst(Header.CONTENT_TYPE);
        return contentType == null ? null : contentType.toLowerCase(Locale.ENGLISH);
    }

    public static Optional<String> getAuthorizationHeader(Request request) {
        Objects.requireNonNull(request, Required.REQUEST);

        String authorization = request.getHeader(Header.AUTHORIZATION);
        if (StringUtils.isNotBlank(authorization)) {
            authorization = authorization.replace("Bearer", "").trim();
        }

        return Optional.ofNullable(authorization);
    }
}