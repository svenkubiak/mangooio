package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.authentication.Authentication;
import io.mangoo.filters.AuthenticationFilter;
import io.mangoo.routing.Response;

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