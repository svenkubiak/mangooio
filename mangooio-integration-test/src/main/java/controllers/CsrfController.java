package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.CsrfFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Session;

public class CsrfController {
    public Response csrf(Session session) {
        session.keep();
        return Response.ok();
    }

    @FilterWith(CsrfFilter.class)
    public Response valid() {
        return Response.ok().render("foo", "bar");
    }
    
    @FilterWith(CsrfFilter.class)
    public Response invalid() {
        return Response.ok().render("foo", "bar");
    }
}