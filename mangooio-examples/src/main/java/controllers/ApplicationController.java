package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.AuthenticationFilter;
import io.mangoo.routing.Response;

/**
 *
 * @author svenkubiak
 *
 */
@FilterWith(AuthenticationFilter.class)
public class ApplicationController {
    public Response index() {
        return Response.withOk();
    }
}