package filters;

import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.bindings.Request;

public class MyGlobalFilter implements MangooRequestFilter {

    @Override
    public boolean continueRequest(Request request) {
        return true;
    }

}
