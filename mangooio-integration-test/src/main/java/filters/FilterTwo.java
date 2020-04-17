package filters;

import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

/**
 * 
 * @author svenkubiak
 *
 */
public class FilterTwo implements PerRequestFilter{
    @Override
    public Response execute(Request request, Response response) {
        request.addAttribute("filtertwo", "filtertwo");
        return response;
    }
}