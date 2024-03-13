package controllers;

import com.google.inject.Inject;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.routing.Response;
import models.Person;

import java.util.List;
import java.util.Objects;

public class ApplicationController {
    private Datastore datastore;
    
    @Inject
    public ApplicationController(Datastore datastore) {
        this.datastore = Objects.requireNonNull(datastore, "datastore can not be null");
    }
    
    public Response index() {
        String hello = "Hello World!";
        return Response.withOk().andContent("hello", hello);
    }
    
    public Response persons() {
        List<Person> persons = this.datastore.findAll(Person.class);
        
        return Response.withOk().andContent("persons", persons);
    }
}