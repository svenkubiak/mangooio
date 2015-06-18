package controllers;

import io.undertow.util.HttpString;

import java.io.File;
import java.net.URL;

import mangoo.io.routing.Response;

public class ApplicationController {

    public Response index() {
        return Response.withOk();
    }

    public Response redirect() {
        return Response.withRedirect("/");
    }

    public Response text() {
        return Response.withOk().andTextBody("foo");
    }

    public Response forbidden() {
        return Response.withForbidden().andEmptyBody();
    }

    public Response badrequest() {
        return Response.withBadRequest().andEmptyBody();
    }

    public Response unauthorized() {
        return Response.withUnauthorized().andEmptyBody();
    }

    public Response binary() {
        URL url = this.getClass().getResource("/attachment.txt");
        File file = new File(url.getFile());

        return Response.withOk().andBinaryFile(file);
    }

    public Response header() {
        return Response
                .withOk()
                .andEmptyBody()
                .andHeader(new HttpString("Access-Control-Allow-Origin"), "https://mangoo.io");
    }
}