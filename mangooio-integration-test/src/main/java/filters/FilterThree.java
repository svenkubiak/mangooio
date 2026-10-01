package filters;

import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class FilterThree implements PerRequestFilter {
    @Override
    public Response execute(Request request, Response response) {
        request.addAttribute("filterthree", "filterthree");
        return response;
    }
}