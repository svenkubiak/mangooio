package controllers;

import mangoo.io.routing.Response;

public class ApplicationController {
    
    public Response index() {
        return Response.withOk();
    }
    
    public Response redirect() {
        return Response.withRedirect("/");
    }
    
    public Response text() {
        return Response.withOk().andTextBody("foo");
    }
    
    public Response forbidden() {
        return Response.withForbidden().andEmptyBody();
    }
    
    public Response badrequest() {
        return Response.withBadRequest().andEmptyBody();
    }
    
    public Response unauthorized() {
        return Response.withUnauthorized().andEmptyBody();
    }
}