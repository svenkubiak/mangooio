package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Session;

public class SessionController {
    public Response session(Session session) {
        session.add("foo", "this is a session value");
        
        return Response.withOk().andEmptyBody();
    }
}
