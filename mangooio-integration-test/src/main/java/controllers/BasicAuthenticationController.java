package controllers;

import io.mangoo.routing.Response;

/**
 * 
 * @author svenkubiak
 *
 */

public class BasicAuthenticationController {
    public Response basicauth() {
        return Response.ok().bodyText("authenticated");
    }
}