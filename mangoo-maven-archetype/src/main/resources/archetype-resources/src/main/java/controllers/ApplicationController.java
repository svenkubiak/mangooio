package controllers;

import mangoo.io.routing.Result;
import mangoo.io.routing.Results;

public class ApplicationController {
    
    public Result index() {
        String hello = "Hello World!";
        
        return Results.ok().render("hello", hello);
    }
}