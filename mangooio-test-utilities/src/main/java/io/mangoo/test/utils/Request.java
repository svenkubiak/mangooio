package io.mangoo.test.utils;

import com.google.common.base.Preconditions;

import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public final class Request {
    private static final String PRECONTIION_ERROR = "URI can not be null";

    private Request() {
    }

    /**
     * Creates a new HTTP GET request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response get(String uri) {
        Preconditions.checkNotNull(uri, PRECONTIION_ERROR);
        
        return new Response(uri, Methods.GET);
    }
    
    /**
     * Creates a new HTTP POST request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response post(String uri) {
        Preconditions.checkNotNull(uri, PRECONTIION_ERROR);
        
        return new Response(uri, Methods.POST);
    }

    /**
     * Creates a new HTTP PUT request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response put(String uri) {
        Preconditions.checkNotNull(uri, PRECONTIION_ERROR);
        
        return new Response(uri, Methods.PUT);
    }
    
    /**
     * Creates a new HTTP DELETE request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response delete(String uri) {
        Preconditions.checkNotNull(uri, PRECONTIION_ERROR);
        
        return new Response(uri, Methods.DELETE);
    }
}