package filters;

import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class MyGlobalFilter implements MangooRequestFilter {

    @Override
    public Response execute(Request request, Response response) {
        return response;
    }
}