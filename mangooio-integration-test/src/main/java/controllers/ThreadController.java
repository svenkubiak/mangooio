package controllers;

import io.mangoo.routing.Response;

public class ThreadController {
    public Response name() {
        return Response.ok().bodyText(Thread.currentThread().getName());
    }
}
