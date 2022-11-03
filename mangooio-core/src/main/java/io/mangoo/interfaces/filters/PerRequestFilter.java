package io.mangoo.interfaces.filters;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

@FunctionalInterface
public interface PerRequestFilter {
    Response execute(Request request, Response response);
}