package controllers;

import mangoo.io.routing.Response;
import mangoo.io.routing.bindings.Form;

public class FormController {
    public Response form(Form form) {
        return Response.withOk().andContent("form", form);
    }
}
