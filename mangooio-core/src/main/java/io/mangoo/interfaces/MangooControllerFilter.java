package io.mangoo.interfaces;

import io.mangoo.routing.bindings.Exchange;

/**
 *
 * @author svenkubiak
 *
 * @deprecated As of release 1.1.0, replaced by MangooFilter
 *
 */
@FunctionalInterface
@Deprecated
public interface MangooControllerFilter {
    /**
     * Executes a filter before a request controller or method
     *
     * @param exchange An exchange object containing request informations
     * @return True if the request should continue, false if the request should stop after this filter
     *
     */
    @Deprecated
    public boolean filter(Exchange exchange);
}