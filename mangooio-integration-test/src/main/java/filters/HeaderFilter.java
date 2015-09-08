package filters;

import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.Headers;

public class HeaderFilter implements MangooFilter {
    @Override
    public Response execute(Request request, Response response) {
        response.andHeader(Headers.CONTENT_MD5, "12");

        return response;
    }
}