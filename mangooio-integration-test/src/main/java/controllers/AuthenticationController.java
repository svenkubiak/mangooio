package controllers;

import mangoo.io.annotations.FilterWith;
import mangoo.io.authentication.Authentication;
import mangoo.io.filters.AuthenticationFilter;
import mangoo.io.routing.Response;

public class AuthenticationController {
    
    @FilterWith(AuthenticationFilter.class)
    public Response notauthenticated() {
        return Response.withOk().andEmptyBody();
    }
    
    public Response login(Authentication authentication) {
        authentication.login("user", false);
        return Response.withOk().andEmptyBody();
    }
    
    public Response logout(Authentication authentication) {
        authentication.logout();
        return Response.withOk().andEmptyBody();
    }
}