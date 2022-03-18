package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.JsonUtils;
import models.Person;

public class JsonController {
    private static final int AGE = 24;

    public Response render() {
        var person = new Person("Peter", "Parker", AGE);
        return Response.withOk().andJsonBody(person);
    }
    
    public Response jsonBody() {
        var person = new Person("Peter", "Parker", AGE);
        String json = JsonUtils.toJson(person);
        
        return Response.withOk().andJsonBody(json);
    }

    public Response parse(Person person) {
        return Response.withOk().andContent("person", person);
    }

    public Response body(Request request) {
        return Response.withOk().andTextBody(request.getURI());
    }

    public Response requestAndJson(Request request, Person person) {
        return Response.withOk().andTextBody(request.getURI() + person.getFirstname());
    }
}