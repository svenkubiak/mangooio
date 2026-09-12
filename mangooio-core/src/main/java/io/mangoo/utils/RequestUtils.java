package io.mangoo.utils;

import io.mangoo.constants.Header;
import io.mangoo.constants.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Request;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Methods;
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
     * Converts request and query parameter into a single map
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A single map contain both request and query parameter
     */
    public static Map<String, String> getRequestParameters(HttpServerExchange exchange) {
        Objects.requireNonNull(exchange, Required.HTTP_SERVER_EXCHANGE);

        final Map<String, String> requestParameter = new HashMap<>();
        exchange.getQueryParameters().forEach((key, value) -> requestParameter.put(key, value.element()));
        exchange.getPathParameters().forEach((key, value) -> requestParameter.put(key, value.element()));

        return requestParameter;
    }

    /**
     * Checks if any query parameter is present more than once in the raw query string,
     * e.g. {@code ?id=1&id=2}.
     * <p>
     * As {@link #getRequestParameters(HttpServerExchange)} collapses each parameter to a single
     * value, requests with duplicated query parameters are ambiguous and should be rejected to
     * avoid HTTP parameter pollution.
     * <p>
     * The raw query string is used deliberately: Undertow merges path template values (e.g. from a
     * route {@code /foo/{id}}) into the parsed query parameter map, which would otherwise cause
     * legitimate requests to be flagged as duplicates.
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