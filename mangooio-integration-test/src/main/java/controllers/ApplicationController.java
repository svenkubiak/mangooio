package controllers;

import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.mangoo.core.Application;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import models.Person;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class ApplicationController {
    
    @Inject
    @Named("application.named")
    private String named;
    
    public Response index() {
        return Response.ok();
    }

    public Response person() {
        Person person = new Person("foo", "bar", 42); //NOSONAR
        Application.getInstance(Datastore.class).save(person);
        return Response.redirect("/");
    }
    
    @SuppressWarnings("null")
    public Response error() {
        String foo = null;
        foo.length(); //NOSONAR
        
        return Response.ok();
    }
    
    public Response route() {
        return Response.ok().render();
    }
    
    public Response api() {
        return Response.ok();
    }

    public Response redirect() {
        return Response.redirect("/");
    }
    
    public Response text() {
        return Response.ok().bodyText("foo");
    }
    
    public Response named() {
        return Response.ok().bodyText(named);
    }
    
    public Response limit() {
        return Response.ok();
    }
    
    public Response reverse() {
        return Response.ok().render();
    }
    
    public Response prettytime() {
        var localDateTime = LocalDateTime.now();
        var localDate = LocalDate.now();
        Date date = new Date(); //NOSONAR
        
        return Response.ok()
                .render("localDateTime", localDateTime)
                .render("localDate", localDate)
                .render("date", date); //NOSONAR
    }

    public Response forbidden() {
        return Response.forbidden();
    }
    
    public Response unrenderedText() {
        String body = null;
        try {
            body = Resources.toString(Resources.getResource("templates/ApplicationController/unrenderedText.html"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Intentionally left blank
        }

        return Response.ok().bodyHtml(body);
    }
    
    public Response badrequest() {
        return Response.badRequest();
    }

    public Response unauthorized() {
        return Response.unauthorized();
    }

    public Response request(Request request) {
        return Response.ok().bodyText(request.getURI());
    }

    public Response post(Request request) {
        return Response.ok().bodyText(request.getBody());
    }

    public Response patch(Request request) {
        return Response.ok().bodyText(request.getBody());
    }
    
    public Response put(Request request) {
        return Response.ok().bodyText(request.getBody());
    }

    public Response jsonBoonPost(Request request) {
        return Response.ok().bodyText(request.getBodyAsJsonMap().toString());
    }

    public Response jsonBoonPut(Request request) {
        return Response.ok().bodyText(request.getBodyAsJsonMap().toString());
    }
    
    public Response location(String myloc) {
        return Response.ok().render("myloc", myloc);
    }
    
    public Response controller() {
        return Response.ok().template("/ApplicationController/location.ftl");
    }
    
    public Response freemarker() {
        return Response.ok().render();
    }

    public Response header() {
        return Response
                .ok()
                .header("Access-Control-Allow-Origin", "https://mangoo.io");
    }

    public Response default200() {
        return Response.ok().bodyDefault();
    }

    public Response default400() {
        return Response.badRequest().bodyDefault();
    }

    public Response default401() {
        return Response.unauthorized().bodyDefault();
    }

    public Response default403() {
        return Response.forbidden().bodyDefault();
    }

    public Response default404() {
        return Response.notFound().bodyDefault();
    }

    public Response default500() {
        return Response.internalServerError().bodyDefault();
    }

    public Response defaultXXX() {
        return Response.status(429).bodyDefault();
    }
}