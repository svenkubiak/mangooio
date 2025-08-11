package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.CsrfFilter;
import io.mangoo.routing.Response;

public class CsrfController {
    public Response form() {
        return Response.ok().render("foo", "bar");
    }
    
    public Response token() {
        return Response.ok().render("foo", "bar");
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