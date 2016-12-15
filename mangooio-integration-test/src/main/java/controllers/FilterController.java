package controllers;

import filters.ContentFilter;
import filters.FilterOne;
import filters.FilterThree;
import filters.FilterTwo;
import filters.HeaderFilter;
import io.mangoo.annotations.FilterWith;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class FilterController {

    @FilterWith(ContentFilter.class)
    public Response filter() {
        return Response.withOk();
    }

    @FilterWith(HeaderFilter.class)
    public Response headerfilter() {
        return Response.withOk().andEmptyBody();
    }
    
    @FilterWith({FilterOne.class, FilterTwo.class, FilterThree.class})
    public Response filters(Request request) {
        String one = (String) request.getAttribute("filterone");
        String two = (String) request.getAttribute("filtertwo");
        String three = (String) request.getAttribute("filterthree");
        
        return Response.withOk().andTextBody(one + two + three);
    }
}