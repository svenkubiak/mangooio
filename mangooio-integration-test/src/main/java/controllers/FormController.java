package controllers;

import mangoo.io.routing.Response;
import mangoo.io.routing.bindings.Form;

public class FormController {
    private static final int MIN_SIZE = 11;
    private static final int MAX_SIZE = 12;

    public Response form() {
        return Response.withOk();
    }

    public Response validateform(Form form) {
        form.required("name");
        form.email("email");
        form.exactMatch("password", "passwordconfirm"); //NOSONAR
        form.match("email2", "email2confirm");
        form.ipv4("ipv4");
        form.ipv6("ipv6");
        form.max(MAX_SIZE, "phone");
        form.min(MIN_SIZE, "fax");

        if (!form.hasErrors()) {
            return Response.withOk().andTextBody("Fancy that!");
        }

        return Response.withOk();
    }
}