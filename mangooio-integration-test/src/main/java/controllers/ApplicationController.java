package controllers;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.HttpString;

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

    public Response etag() {
        return Response.withOk().andTextBody("foo").andEtag();
    }

    public Response binary() {
        URL url = this.getClass().getResource("/attachment.txt");
        File file = new File(url.getFile());

        return Response.withOk().andBinaryFile(file);
    }

    public Response request(Request request) {
        return Response.withOk().andTextBody(request.getURI());
    }
    
    public Response post(Request request) {
        return Response.withOk().andTextBody(request.getBody());
    }

    public Response put(Request request) {
        return Response.withOk().andTextBody(request.getBody());
    }

    public Response jsonPathPost(Request request) {
        return Response.withOk().andTextBody(request.getBodyAsJsonPath().jsonString());
    }
    
    public Response jsonPathPut(Request request) {
        return Response.withOk().andTextBody(request.getBodyAsJsonPath().jsonString());
    }
    
    public Response jsonBoonPost(Request request) {
        return Response.withOk().andTextBody(request.getBodyAsJsonMap().toString());
    }
    
    public Response jsonBoonPut(Request request) {
        return Response.withOk().andTextBody(request.getBodyAsJsonMap().toString());
    }
    
    public Response header() {
        return Response
                .withOk()
                .andEmptyBody()
                .andHeader(new HttpString("Access-Control-Allow-Origin"), "https://mangoo.io");
    }

    public Response localdate(LocalDate localDate) {
        return Response
                .withOk()
                .andTextBody(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public Response localdatetime(LocalDateTime localDateTime) {
        return Response
                .withOk()
                .andTextBody(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}