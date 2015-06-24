package controllers;

import mangoo.io.annotations.FilterWith;
import mangoo.io.routing.Response;
import filters.ContentFilter;

public class FilterController {

    @FilterWith(ContentFilter.class)
    public Response filter() {
        return Response.withOk();
    }
}
