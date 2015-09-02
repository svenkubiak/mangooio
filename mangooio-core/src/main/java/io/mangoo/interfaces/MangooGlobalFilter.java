package io.mangoo.interfaces;

import io.mangoo.routing.bindings.Exchange;

/**
 *
 * @author svenkubiak
 *
 * @deprecated As of release 1.1.0, replaced by #MangooRequestFilter
 *
 */
@FunctionalInterface
@Deprecated
public interface MangooGlobalFilter {
    /**
     * Executes a filter before every request
     *
     * @param exchange An exchange object containing request informations
     * @return True if the request should continue, false if the request should stop after this filter
     *
     */
    @Deprecated
    public boolean filter(Exchange exchange);
}