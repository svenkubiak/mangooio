package filters;

import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class MyGlobalFilter implements OncePerRequestFilter {

    @Override
    public Response execute(Request request, Response response) {
        return response;
    }
}