package io.mangoo.utils;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.routing.Response;
import io.mangoo.routing.handlers.BinaryHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public final class RequestUtils {

    public static Map<String, String> getRequestParameters(HttpServerExchange exchange) {
        Map<String, String> requestParamater = new HashMap<String, String>();

        Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        queryParameters.putAll(exchange.getPathParameters());
        queryParameters.entrySet().forEach(entry -> requestParamater.put(entry.getKey(), entry.getValue().element()));

        return requestParamater;
    }

    public static boolean isPostOrPut(HttpServerExchange exchange) {
        return (Methods.POST).equals(exchange.getRequestMethod()) || (Methods.PUT).equals(exchange.getRequestMethod());
    }

    public static void handleResponse(HttpServerExchange exchange, Response response) {
        if (response.isRedirect()) {
            handleRedirectResponse(exchange, response);
        } else if (response.isBinary()) {
            handleBinaryResponse(exchange, response);
        } else {
            handleRenderedResponse(exchange, response);
        }
    }

    private static void handleRenderedResponse(HttpServerExchange exchange, Response response) {
        exchange.setResponseCode(response.getStatusCode());
        exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), Default.X_XSS_PPROTECTION.toInt());
        exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), Default.NOSNIFF.toString());
        exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), Default.SAMEORIGIN.toString());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, response.getContentType() + "; charset=" + response.getCharset());
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());

        if (response.isETag()) {
            exchange.getResponseHeaders().put(Headers.ETAG, DigestUtils.md5Hex(response.getBody()));
        }

        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.getResponseSender().send(response.getBody());
    }

    private static void handleBinaryResponse(HttpServerExchange exchange, Response response) {
        exchange.dispatch(exchange.getDispatchExecutor(), new BinaryHandler(response));
    }

    private static void handleRedirectResponse(HttpServerExchange exchange, Response response) {
        exchange.setResponseCode(StatusCodes.FOUND);
        exchange.getResponseHeaders().put(Headers.LOCATION, response.getRedirectTo());
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        exchange.endExchange();
    }
}