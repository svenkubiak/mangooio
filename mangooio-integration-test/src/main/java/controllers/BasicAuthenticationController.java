package controllers;

import io.mangoo.routing.Response;

/**
 * 
 * @author svenkubiak
 *
 */

public class BasicAuthenticationController {
    public Response basicauth() {
        return Response.withOk().andTextBody("authenticated");
    }
    
    public Response basicauth2fa() {
        return Response.withOk().andTextBody("authenticated");
    }
}