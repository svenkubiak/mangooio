package io.mangoo.utils;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mangoo.enums.Binding;
import io.mangoo.enums.ContentType;
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
    public static List<Binding> bindings = Arrays.asList(Binding.values());

    private RequestUtils() {
    }

    public static Map<String, String> getRequestParameters(HttpServerExchange exchange) {
        Map<String, String> requestParamater = new HashMap<String, String>();

        Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        queryParameters.putAll(exchange.getPathParameters());
        queryParameters.entrySet().forEach(entry -> requestParamater.put(entry.getKey(), entry.getValue().element())); //NOSONAR

        return requestParamater;
    }

    public static boolean isPostOrPut(HttpServerExchange exchange) {
        return (Methods.POST).equals(exchange.getRequestMethod()) || (Methods.PUT).equals(exchange.getRequestMethod());
    }

    public static boolean isJSONRequest(HttpServerExchange exchange) {
        HeaderMap requestHeaders = exchange.getRequestHeaders();

        return requestHeaders != null && requestHeaders.get(Headers.CONTENT_TYPE) != null &&
                requestHeaders.get(Headers.CONTENT_TYPE).element().toLowerCase().contains(ContentType.APPLICATION_JSON.toString().toLowerCase());
    }

    public static Binding getBinding(Class<?> clazz) {
        String clazzName = clazz.getName();

        if (Binding.FORM.toString().equals(clazzName)) {
            return Binding.FORM;
        } else if (Binding.SESSION.toString().equals(clazzName)) {
            return Binding.SESSION;
        } else if (Binding.FLASH.toString().equals(clazzName)) {
            return Binding.FLASH;
        } else if (Binding.REQUEST.toString().equals(clazzName)) {
            return Binding.REQUEST;
        } else if (Binding.BODY.toString().equals(clazzName)) {
            return Binding.BODY;
        } else if (Binding.LOCALDATE.toString().equals(clazzName)) {
            return Binding.LOCALDATE;
        } else if (Binding.STRING.toString().equals(clazzName)) {
            return Binding.STRING;
        } else if (Binding.INTEGER.toString().equals(clazzName)) {
            return Binding.INTEGER;
        } else if (Binding.INT.toString().equals(clazzName)) {
            return Binding.INT;
        } else if (Binding.FLOAT.toString().equals(clazzName)) {
            return Binding.FLOAT;
        } else if (Binding.FLOAT_PRIMITIVE.toString().equals(clazzName)) {
            return Binding.FLOAT_PRIMITIVE;
        } else if (Binding.LONG.toString().equals(clazzName)) {
            return Binding.LONG;
        } else if (Binding.DOUBLE.toString().equals(clazzName)) {
            return Binding.DOUBLE;
        } else if (Binding.DOUBLE_PRIMITIVE.toString().equals(clazzName)) {
            return Binding.DOUBLE_PRIMITIVE;
        } else if (Binding.LOCALDATETIME.toString().equals(clazzName)) {
            return Binding.LOCALDATETIME;
        } else if (Binding.AUTHENTICATION.toString().equals(clazzName)) {
            return Binding.AUTHENTICATION;
        }

        return null;
    }
}