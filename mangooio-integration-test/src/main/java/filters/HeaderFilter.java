package filters;

import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class HeaderFilter implements PerRequestFilter {
    @Override
    public Response execute(Request request, Response response) {
        response.header("Content-MD5", "12");

        return response;
    }
}