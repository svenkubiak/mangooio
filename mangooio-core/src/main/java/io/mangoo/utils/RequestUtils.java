package io.mangoo.utils;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;

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
}