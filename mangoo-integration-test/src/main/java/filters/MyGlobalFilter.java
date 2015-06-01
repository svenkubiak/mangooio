package filters;

import mangoo.io.interfaces.MangooRequestFilter;
import mangoo.io.routing.bindings.Exchange;

public class MyGlobalFilter implements MangooRequestFilter {

    @Override
    public boolean filter(Exchange exchange) {
        return true;
    }

}
