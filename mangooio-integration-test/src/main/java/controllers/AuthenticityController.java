package controllers;

import mangoo.io.annotations.FilterWith;
import mangoo.io.filters.AuthenticityFilter;
import mangoo.io.routing.Response;

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