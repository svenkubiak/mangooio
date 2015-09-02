package io.mangoo.interfaces;

import io.mangoo.routing.bindings.Request;

/**
 *
 * @author svenkubiak
 *
 */
@FunctionalInterface
public interface MangooFilter {
    /**
     * Executes a filter on a controller class or method
     *
     * @param request An request object containing request data
     *
     * @return True if the request should continue, false if the request should stop after this filter
     */
    public boolean continueRequest(Request request);
}
