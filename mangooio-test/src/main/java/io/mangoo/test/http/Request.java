package io.mangoo.test.http;

import java.util.Objects;

import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public final class Request {
    private static final String URI_ERROR = "URI can not be null";

    private Request() {
    }

    /**
     * Creates a new HTTP GET request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response get(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new Response(uri, Methods.GET.toString());
    }
    
    /**
     * Creates a new HTTP POST request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response post(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new Response(uri, Methods.POST.toString());
    }

    /**
     * Creates a new HTTP PUT request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response put(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new Response(uri, Methods.PUT.toString());
    }
    
    /**
     * Creates a new HTTP DELETE request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response delete(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new Response(uri, Methods.DELETE.toString());
    }
    
    /**
     * Creates a new HTTP HEAD request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response head(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new Response(uri, Methods.HEAD.toString());
    }
    
    /**
     * Creates a new HTTP PACTH request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response patch(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new Response(uri, Methods.PATCH.toString());
    }
    
    /**
     * Creates a new HTTP OPTIONS request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static Response options(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new Response(uri, Methods.OPTIONS.toString());
    }
}