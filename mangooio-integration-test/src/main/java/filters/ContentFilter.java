package filters;

import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class ContentFilter implements MangooFilter {

    @Override
    public Response execute(Request request, Response response) {
        return response.andContent("foo", "bar");
    }
}