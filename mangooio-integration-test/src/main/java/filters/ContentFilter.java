package filters;

import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.bindings.Request;

public class ContentFilter implements MangooFilter {

    @Override
    public boolean continueRequest(Request request) {
        request.getPayload().addContent("foo", "bar");

        return true;
    }
}