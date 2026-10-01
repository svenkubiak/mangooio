package io.mangoo.test.http;

import io.mangoo.constants.Required;
import io.undertow.util.Methods;

import java.util.Objects;

public final class TestRequest {
    private TestRequest() {
    }

    public static TestResponse create(String uri, String method) {
        Objects.requireNonNull(uri, Required.URI);
        Objects.requireNonNull(method, Required.HTTP_METHOD);
        
        return new TestResponse(uri, method);
    }
    
    public static TestResponse get(String uri) {
        Objects.requireNonNull(uri, Required.URI);
        
        return new TestResponse(uri, Methods.GET.toString());
    }
    
    public static TestResponse post(String uri) {
        Objects.requireNonNull(uri, Required.URI);
        
        return new TestResponse(uri, Methods.POST.toString());
    }

    public static TestResponse put(String uri) {
        Objects.requireNonNull(uri, Required.URI);
        
        return new TestResponse(uri, Methods.PUT.toString());
    }
    
    public static TestResponse delete(String uri) {
        Objects.requireNonNull(uri, Required.URI);
        
        return new TestResponse(uri, Methods.DELETE.toString());
    }
    
    public static TestResponse head(String uri) {
        Objects.requireNonNull(uri, Required.URI);
        
        return new TestResponse(uri, Methods.HEAD.toString());
    }
    
    public static TestResponse patch(String uri) {
        Objects.requireNonNull(uri, Required.URI);
        
        return new TestResponse(uri, Methods.PATCH.toString());
    }
    
    public static TestResponse options(String uri) {
        Objects.requireNonNull(uri, Required.URI);
        
        return new TestResponse(uri, Methods.OPTIONS.toString());
    }
}