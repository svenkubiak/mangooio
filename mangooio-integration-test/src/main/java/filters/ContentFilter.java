package filters;

import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class ContentFilter implements PerRequestFilter {

    @Override
    public Response execute(Request request, Response response) {
        return response.andContent("foo", "bar");
    }
}