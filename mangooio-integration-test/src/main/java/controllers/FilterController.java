package controllers;

import filters.*;
import io.mangoo.annotations.FilterWith;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class FilterController {

    @FilterWith(ContentFilter.class)
    public Response filter() {
        return Response.ok().render();
    }

    @FilterWith(HeaderFilter.class)
    public Response headerfilter() {
        return Response.ok();
    }
    
    @FilterWith({FilterOne.class, FilterTwo.class, FilterThree.class})
    public Response filters(Request request) {
        String one = (String) request.getAttribute("filterone");
        String two = (String) request.getAttribute("filtertwo");
        String three = (String) request.getAttribute("filterthree");
        
        return Response.ok().bodyText(one + two + three);
    }
}