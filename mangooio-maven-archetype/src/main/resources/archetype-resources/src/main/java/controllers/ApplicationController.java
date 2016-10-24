package controllers;

import io.mangoo.routing.Response;

public class ApplicationController {
    public Response index() {
        String hello = "Hello World!";
        return Response.withOk().andContent("hello", hello);
    }
}
