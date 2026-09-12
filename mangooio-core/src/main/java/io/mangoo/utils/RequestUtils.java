package io.mangoo.utils;

import io.mangoo.constants.Header;
import io.mangoo.constants.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Request;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Methods;
import io.undertow.util.PathTemplateMatch;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public final class RequestUtils {
    private static final Logger LOG = LogManager.getLogger(RequestUtils.class);
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
     * Returns the route parameters of a request, e.g. {@code id} of a route {@code /foo/{id}}.
     * <p>
     * These are resolved by the router and can not be forged by a client, which makes them the
     * only parameters an authorization decision may be based on.
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A map containing the route parameters of the request
     */
    public static Map<String, String> getPathParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        final Map<String, String> pathParameter = new HashMap<>();

        // Handlers that use addPathParam, e.g. PathTemplateHandler
        exchange.getPathParameters().forEach((key, value) -> pathParameter.put(key, value.element()));

        // The PathTemplateMatch of the router is authoritative and therefore applied last
        var pathTemplateMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        if (pathTemplateMatch != null) {
            pathParameter.putAll(pathTemplateMatch.getParameters());
        }

        return pathParameter;
    }

    /**
     * Returns the query parameters of a request, e.g. {@code limit} of {@code ?limit=25}.
     * <p>
     * These are always untrusted client input.
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A map containing the query parameters of the request
     */
    public static Map<String, String> getQueryParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        final Map<String, String> queryParameter = new HashMap<>();
        exchange.getQueryParameters().forEach((key, value) -> queryParameter.put(key, value.element()));

        return queryParameter;
    }

    /**
     * Converts route and query parameter into a single map, where a route parameter always wins
     * over a query parameter of the same name
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A single map containing both route and query parameter
     */
    public static Map<String, String> getRequestParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        final Map<String, String> requestParameter = getQueryParameters(exchange);
        requestParameter.putAll(getPathParameters(exchange));

        return requestParameter;
    }

    /**
     * Checks if a query parameter carries the same name as a route parameter of the matched route,
     * e.g. {@code /foo/abc?id=xyz} on a route {@code /foo/{id}}.
     * <p>
     * Such a request is ambiguous about which value the client meant. The route value wins, but an
     * application may want to reject the request instead.
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if a query parameter collides with a route parameter, false otherwise
     */
    public static boolean hasAmbiguousParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        Map<String, String> pathParameter = getPathParameters(exchange);
        if (pathParameter.isEmpty()) {
            return false;
        }

        return pathParameter.keySet().stream().anyMatch(exchange.getQueryParameters()::containsKey);
    }

    /**
     * Checks if any query parameter is present more than once in the raw query string,
     * e.g. {@code ?id=1&id=2}.
     * <p>
     * As {@link #getQueryParameters(HttpServerExchange)} collapses each parameter to a single
     * value, requests with duplicated query parameters are ambiguous and should be rejected to
     * avoid HTTP parameter pollution.
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if at least one query parameter key appears more than once, false otherwise
     */
    public static boolean hasMultipleParameterValues(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        String queryString = exchange.getQueryString();
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
            if (!seenKeys.add(key)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the request is a POST, PUT or PATCH request
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request is a POST, PUT or PATCH request, false otherwise
     */
    public static boolean isPostPutPatch(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        return (Methods.POST).equals(exchange.getRequestMethod()) || (Methods.PUT).equals(exchange.getRequestMethod()) || (Methods.PATCH).equals(exchange.getRequestMethod());
    }
    
    /**
     * Checks if the requests content-type contains application/json
     *
     * @param exchange The Undertow HttpServerExchange
     * @return True if the request content-type contains application/json, false otherwise
     */
    public static boolean isJsonRequest(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        String contentType = exchange.getRequestHeaders().getFirst(Header.CONTENT_TYPE);
        if (contentType == null) {
            return false;
        }

        contentType = contentType.toLowerCase(Locale.ENGLISH);

        return contentType.startsWith("application/json") || contentType.contains("+json");
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