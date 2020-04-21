package io.mangoo.admin;

import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class AdminFilter implements PerRequestFilter {
    @Override
    public Response execute(Request request, Response response) {
        return Response.withRedirect("/@admin/login").end();
    }
}