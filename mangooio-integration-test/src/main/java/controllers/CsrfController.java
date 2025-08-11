package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.CsrfFilter;
import io.mangoo.routing.Response;

public class CsrfController {

    public Response form() {
        return Response.ok().render();
    }

    public Response token() {
        return Response.ok().render();
    }

    @FilterWith(CsrfFilter.class)
    public Response validate() {
        return Response.ok().render("foo", "bar");
    }
}