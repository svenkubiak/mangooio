package io.mangoo.utils.http;

import java.util.Objects;

import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public final class HTTPRequest {
    private static final String URI_ERROR = "URI can not be null";

    private HTTPRequest() {
    }

    /**
     * Creates a new HTTP GET request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static HTTPResponse get(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new HTTPResponse(uri, Methods.GET);
    }
    
    /**
     * Creates a new HTTP POST request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static HTTPResponse post(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new HTTPResponse(uri, Methods.POST);
    }

    /**
     * Creates a new HTTP PUT request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static HTTPResponse put(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new HTTPResponse(uri, Methods.PUT);
    }
    
    /**
     * Creates a new HTTP DELETE request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static HTTPResponse delete(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new HTTPResponse(uri, Methods.DELETE);
    }
}