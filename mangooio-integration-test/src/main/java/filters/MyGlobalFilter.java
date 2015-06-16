package filters;

import mangoo.io.interfaces.MangooGlobalFilter;
import mangoo.io.routing.bindings.Exchange;

public class MyGlobalFilter implements MangooGlobalFilter {

    @Override
    public boolean filter(Exchange exchange) {
        return true;
    }

}
