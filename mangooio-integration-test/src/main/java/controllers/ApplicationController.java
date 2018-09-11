package controllers;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.HttpString;

public class ApplicationController {
    
    public Response index() {
        return Response.withOk();
    }
    
    public Response route() {
        return Response.withOk();
    }

    public Response redirect() {
        return Response.withRedirect("/");
    }
    
    public Response text() {
        return Response.withOk().andTextBody("foo");
    }
    
    public Response limit() {
        return Response.withOk().andEmptyBody();
    }
    
    public Response reverse() {
        return Response.withOk();
    }
    
    public Response prettytime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDate localDate = LocalDate.now();
        Date date = new Date(); //NOSONAR
        
        return Response.withOk()
                .andContent("localDateTime", localDateTime)
                .andContent("localDate", localDate)
                .andContent("date", date); //NOSONAR
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

    @SuppressWarnings("all")
    public Response binary() {
        final URL url = this.getClass().getResource("/attachment.txt");
        final File file = new File(url.getFile());
        
        return Response.withOk().andBinaryFile(file);
    }

    public Response request(Request request) {
        return Response.withOk().andTextBody(request.getURI());
    }

    public Response post(Request request) {
        return Response.withOk().andTextBody(request.getBody());
    }

    public Response patch(Request request) {
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
    
    public Response location(String myloc) {
        return Response.withOk().andContent("myloc", myloc);
    }
    
    public Response freemarker() {
        return Response.withOk();
    }

    public Response header() {
        return Response
                .withOk()
                .andEmptyBody()
                .andHeader(new HttpString("Access-Control-Allow-Origin"), "https://mangoo.io");
    }
}