package mangoo.io.interfaces;

import mangoo.io.routing.bindings.Exchange;

/**
 *
 * @author svenkubiak
 *
 */
@FunctionalInterface
public interface MangooControllerFilter {
    /**
     * Executes a filter before a request controller or method
     *
     * @param exchange An exchange object containing request informations
     * @return True if the request should continue, false if the request schould stop after this filter
     */
    public boolean filter(Exchange exchange);
}