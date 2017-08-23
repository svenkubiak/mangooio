package controllers;

import io.mangoo.routing.Response;

/**
 *
 * @author svenkubiak
 *
 */
public class ApplicationController {
    
    public Response index() {
        return Response.withOk();
    }
}