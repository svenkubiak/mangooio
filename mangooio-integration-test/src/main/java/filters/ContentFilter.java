package filters;

import mangoo.io.interfaces.MangooControllerFilter;
import mangoo.io.routing.bindings.Exchange;

public class ContentFilter implements MangooControllerFilter {

    @Override
    public boolean filter(Exchange exchange) {
        exchange.addContent("foo", "bar");

        return true;
    }
}