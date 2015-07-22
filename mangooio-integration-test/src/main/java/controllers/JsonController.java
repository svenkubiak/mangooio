package controllers;

import io.mangoo.routing.Response;
import models.Person;

public class JsonController {
    private static final int AGE = 24;

    public Response render() {
        Person person = new Person("Peter", "Parker", AGE);
        return Response.withOk().andJsonBody(person);
    }
    
    public Response parse(Person person) {
        return Response.withOk().andContent("person", person);
    }
}