package filters;

import io.mangoo.interfaces.MangooGlobalFilter;
import io.mangoo.routing.bindings.Exchange;

public class MyGlobalFilter implements MangooGlobalFilter {

    @Override
    public boolean filter(Exchange exchange) {
        return true;
    }

}
