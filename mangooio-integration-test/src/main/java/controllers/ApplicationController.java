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
        return Response.withOk();
    }
    
    @SuppressWarnings("null")
    public Response error() {
        String foo = null;
        foo.length(); //NOSONAR
        
        return Response.withOk();
    }
    
    public Response route() {
        return Response.withOk();
    }
    
    public Response api() {
        return Response.withOk().andEmptyBody();
    }

    public Response redirect() {
        return Response.withRedirect("/");
    }
    
    public Response text() {
        return Response.withOk().andTextBody("foo");
    }
    
    public Response named() {
        return Response.withOk().andTextBody(named);
    }
    
    public Response limit() {
        return Response.withOk().andEmptyBody();
    }
    
    public Response reverse() {
        return Response.withOk();
    }
    
    public Response prettytime() {
        var localDateTime = LocalDateTime.now();
        var localDate = LocalDate.now();
        Date date = new Date(); //NOSONAR
        
        return Response.withOk()
                .andContent("localDateTime", localDateTime)
                .andContent("localDate", localDate)
                .andContent("date", date); //NOSONAR
    }

    public Response forbidden() {
        return Response.withForbidden().andEmptyBody();
    }
    
    public Response unrenderedText() {
        String body = null;
        try {
            body = Resources.toString(Resources.getResource("templates/ApplicationController/unrenderedText.html"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Intentionally left blank
        }

        return Response.withOk().andHtmlBody(body);
    }
    
    public Response badrequest() {
        return Response.withBadRequest().andEmptyBody();
    }

    public Response unauthorized() {
        return Response.withUnauthorized().andEmptyBody();
    }

    public Response binary() throws URISyntaxException {
        final var url = this.getClass().getResource("/attachment.txt");
        final var file = Paths.get(url.toURI());
        
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

    public Response jsonBoonPost(Request request) {
        return Response.withOk().andTextBody(request.getBodyAsJsonMap().toString());
    }

    public Response jsonBoonPut(Request request) {
        return Response.withOk().andTextBody(request.getBodyAsJsonMap().toString());
    }
    
    public Response location(String myloc) {
        return Response.withOk().andContent("myloc", myloc);
    }
    
    public Response controller() {
        return Response.withOk().andTemplate("/ApplicationController/location.ftl");
    }
    
    public Response freemarker() {
        return Response.withOk();
    }

    public Response header() {
        return Response
                .withOk()
                .andEmptyBody()
                .andHeader("Access-Control-Allow-Origin", "https://mangoo.io");
    }
}