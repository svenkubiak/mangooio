package controllers.subcontrollers;

import io.mangoo.routing.Response;

public class SubController {
    public Response check() {
        return Response.ok().bodyEmpty();
    }
}