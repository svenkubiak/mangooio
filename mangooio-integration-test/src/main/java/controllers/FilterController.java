package controllers;

import filters.ContentFilter;
import io.mangoo.annotations.FilterWith;
import io.mangoo.routing.Response;

public class FilterController {

    @FilterWith(ContentFilter.class)
    public Response filter() {
        return Response.withOk();
    }
}
