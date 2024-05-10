package controllers;

import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
    
    @SuppressWarnings("null")
    public Response error() {
        String foo = null;
        foo.length(); //NOSONAR
        
        return Response.ok();
    }
    
    public Response route() {
        return Response.ok();
    }
    
    public Response api() {
        return Response.ok().bodyEmpty();
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
        return Response.ok().bodyEmpty();
    }
    
    public Response reverse() {
        return Response.ok();
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
        return Response.forbidden().bodyEmpty();
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
        return Response.badRequest().bodyEmpty();
    }

    public Response unauthorized() {
        return Response.unauthorized().bodyEmpty();
    }

    public Response binary() throws URISyntaxException {
        final var url = this.getClass().getResource("/attachment.txt");
        final var file = Paths.get(url.toURI());
        
        return Response.ok().andBinaryFile(file);
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
        return Response.ok();
    }

    public Response header() {
        return Response
                .ok()
                .bodyEmpty()
                .header("Access-Control-Allow-Origin", "https://mangoo.io");
    }
}