package filters;

import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

/**
 * 
 * @author svenkubiak
 *
 */
public class FilterThree implements MangooFilter {
    @Override
    public Response execute(Request request, Response response) {
        request.addAttribute("filterthree", "filterthree");
        return response;
    }
}