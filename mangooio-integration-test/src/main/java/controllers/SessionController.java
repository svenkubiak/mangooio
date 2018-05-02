package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Session;

public class SessionController {
    public Response session(Session session) {
        session.put("foo", "this is a session value");

        return Response.withOk().andEmptyBody();
    }
    
    public Response valued(String uuid, Session session) {
        session.put("uuid", uuid);

        return Response.withOk().andEmptyBody();
    }
}