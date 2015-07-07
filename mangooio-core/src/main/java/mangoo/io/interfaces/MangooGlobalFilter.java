package mangoo.io.interfaces;

import mangoo.io.routing.bindings.Exchange;

/**
 *
 * @author svenkubiak
 *
 */
@FunctionalInterface
public interface MangooGlobalFilter {
    /**
     * Executes a filter before every request
     *
     * @param exchange An exchange object containing request informations
     * @return True if the request should continue, false if the request schould stop after this filter
     */
    public boolean filter(Exchange exchange);
}