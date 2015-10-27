package io.mangoo.test;

import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public final class MangooRequest {
    private MangooRequest() {
    }

    /**
     * Creates a new HTTP GET request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static MangooResponse get(String uri) {
        return new MangooResponse(uri, Methods.GET);
    }
    
    /**
     * Creates a new HTTP POST request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static MangooResponse post(String uri) {
        return new MangooResponse(uri, Methods.POST);
    }

    /**
     * Creates a new HTTP PUT request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static MangooResponse put(String uri) {
        return new MangooResponse(uri, Methods.PUT);
    }
    
    /**
     * Creates a new HTTP DELETE request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static MangooResponse delete(String uri) {
        return new MangooResponse(uri, Methods.DELETE);
    }
}