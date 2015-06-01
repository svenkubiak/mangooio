package controllers;

import mangoo.io.routing.Response;
import mangoo.io.routing.bindings.Session;

public class SessionController {
    public Response session(Session session) {
        session.add("foo", "this is a session value");
        
        return Response.withOk().andEmptyBody();
    }
}
