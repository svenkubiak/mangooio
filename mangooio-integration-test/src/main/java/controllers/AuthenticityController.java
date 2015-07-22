package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.AuthenticityFilter;
import io.mangoo.routing.Response;

public class AuthenticityController {
    public Response form() {
        return Response.withOk().andContent("foo", "bar");
    }
    
    public Response token() {
        return Response.withOk().andContent("foo", "bar");
    }
    
    @FilterWith(AuthenticityFilter.class)
    public Response valid() {
        return Response.withOk().andContent("foo", "bar");
    }
    
    @FilterWith(AuthenticityFilter.class)
    public Response invalid() {
        return Response.withOk().andContent("foo", "bar");
    }
}