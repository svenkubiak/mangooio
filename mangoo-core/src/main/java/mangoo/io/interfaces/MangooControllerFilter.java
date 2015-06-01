package mangoo.io.interfaces;

import mangoo.io.routing.bindings.Exchange;

/**
 *
 * @author svenkubiak
 *
 */
@FunctionalInterface
public interface MangooControllerFilter {
    public boolean filter(Exchange exchange);
}