package io.mangoo.test.http;

import io.mangoo.constants.NotNull;
import io.undertow.util.Methods;

import java.util.Objects;

/**
 * 
 * @author svenkubiak
 *
 */
public final class TestRequest {
    private TestRequest() {
    }

    public static TestResponse create(String uri, String method) {
        Objects.requireNonNull(uri, NotNull.URI);
        Objects.requireNonNull(method, NotNull.HTTP_METHOD);
        
        return new TestResponse(uri, method);
    }
    
    /**
     * Creates a new HTTP GET request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static TestResponse get(String uri) {
        Objects.requireNonNull(uri, NotNull.URI);
        
        return new TestResponse(uri, Methods.GET.toString());
    }
    
    /**
     * Creates a new HTTP POST request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static TestResponse post(String uri) {
        Objects.requireNonNull(uri, NotNull.URI);
        
        return new TestResponse(uri, Methods.POST.toString());
    }

    /**
     * Creates a new HTTP PUT request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static TestResponse put(String uri) {
        Objects.requireNonNull(uri, NotNull.URI);
        
        return new TestResponse(uri, Methods.PUT.toString());
    }
    
    /**
     * Creates a new HTTP DELETE request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static TestResponse delete(String uri) {
        Objects.requireNonNull(uri, NotNull.URI);
        
        return new TestResponse(uri, Methods.DELETE.toString());
    }
    
    /**
     * Creates a new HTTP HEAD request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static TestResponse head(String uri) {
        Objects.requireNonNull(uri, NotNull.URI);
        
        return new TestResponse(uri, Methods.HEAD.toString());
    }
    
    /**
     * Creates a new HTTP PACTH request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static TestResponse patch(String uri) {
        Objects.requireNonNull(uri, NotNull.URI);
        
        return new TestResponse(uri, Methods.PATCH.toString());
    }
    
    /**
     * Creates a new HTTP OPTIONS request to the given URI
     * 
     * @param uri The URI to call
     * @return A MangooResponse
     */
    public static TestResponse options(String uri) {
        Objects.requireNonNull(uri, NotNull.URI);
        
        return new TestResponse(uri, Methods.OPTIONS.toString());
    }
}