package controllers;

import java.util.regex.Pattern;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Form;

public class FormController {
    private static final int MIN_SIZE = 11;
    private static final int MAX_SIZE = 12;

    public Response form() {
        return Response.withOk();
    }

    public Response validateform(Form form) {
        form.validation().required("name");
        form.validation().email("email");
        form.validation().exactMatch("password", "passwordconfirm"); //NOSONAR
        form.validation().match("email2", "email2confirm");
        form.validation().ipv4("ipv4");
        form.validation().ipv6("ipv6");
        form.validation().regex("regex", Pattern.compile("[a-z]"));
        form.validation().max("phone", MAX_SIZE);
        form.validation().min("fax", MIN_SIZE);

        if (!form.validation().hasErrors()) {
            return Response.withOk().andTextBody("Fancy that!");
        }

        return Response.withOk();
    }
}