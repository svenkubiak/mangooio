package io.mangoo.interfaces;

import io.mangoo.routing.bindings.Request;

/**
 *
 * @author svenkubiak
 *
 */
@FunctionalInterface
public interface MangooRequestFilter {
    /**
     * Executes a global filter on every mapped request
     *
     * @param request An request object containing request data
     * @param exchange An exchange object for adding response informations
     *
     * @return True if the request should continue, false if the request should stop after this filter
     */
    public boolean continueRequest(Request request);
}
