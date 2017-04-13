package io.mangoo.test.utils;

import java.util.Objects;

import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public final class WebRequest {
    private static final String URI_ERROR = "URI can not be null";

    private WebRequest() {
    }

    /**
     * Creates a new HTTP GET request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static WebResponse get(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new WebResponse(uri, Methods.GET);
    }
    
    /**
     * Creates a new HTTP POST request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static WebResponse post(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new WebResponse(uri, Methods.POST);
    }

    /**
     * Creates a new HTTP PUT request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static WebResponse put(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new WebResponse(uri, Methods.PUT);
    }
    
    /**
     * Creates a new HTTP DELETE request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static WebResponse delete(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new WebResponse(uri, Methods.DELETE);
    }
    
    /**
     * Creates a new HTTP HEAD request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static WebResponse head(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new WebResponse(uri, Methods.HEAD);
    }
    
    /**
     * Creates a new HTTP PACTH request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static WebResponse patch(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new WebResponse(uri, Methods.PATCH);
    }
    
    /**
     * Creates a new HTTP OPTIONS request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static WebResponse options(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);
        
        return new WebResponse(uri, Methods.OPTIONS);
    }
}