package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.JsonUtils;
import models.Person;

public class JsonController {
    private static final int AGE = 24;

    public Response render() {
        var person = new Person("Peter", "Parker", AGE);
        return Response.ok().bodyJson(person);
    }
    
    public Response jsonBody() {
        var person = new Person("Peter", "Parker", AGE);
        String json = JsonUtils.toJson(person);
        
        return Response.ok().bodyJson(json);
    }

    public Response parse(Person person) {
        return Response.ok().render("person", person);
    }

    public Response body(Request request) {
        return Response.ok().bodyText(request.getURI());
    }

    public Response error(Request request) {
        return Response.badRequest().bodyJsonError("An error occurred. Please have a look.");
    }


    public Response requestAndJson(Request request, Person person) {
        return Response.ok().bodyText(request.getURI() + person.getFirstname());
    }
}