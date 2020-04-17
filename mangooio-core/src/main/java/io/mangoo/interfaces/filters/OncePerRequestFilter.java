package io.mangoo.interfaces.filters;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

/**
 *
 * @author svenkubiak
 *
 */
@FunctionalInterface
public interface OncePerRequestFilter {
    Response execute(Request request, Response response);
}