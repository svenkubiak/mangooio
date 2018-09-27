package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Authentication;

public class AuthorizationController {
    public Response read() {
        return Response.withOk().andTextBody("can read");
    }
    
    public Response write() {
        return Response.withOk().andTextBody("can write");
    }
    
    public Response authorize(String subject, Authentication authentication) {
        authentication.login(subject);
        return Response.withOk().andEmptyBody();
    }
}