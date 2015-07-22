package filters;

import io.mangoo.interfaces.MangooControllerFilter;
import io.mangoo.routing.bindings.Exchange;

public class ContentFilter implements MangooControllerFilter {

    @Override
    public boolean filter(Exchange exchange) {
        exchange.addContent("foo", "bar");

        return true;
    }
}