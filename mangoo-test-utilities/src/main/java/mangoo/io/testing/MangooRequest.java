package mangoo.io.testing;

import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public final class MangooRequest {
    private MangooRequest() {
    }

    public static MangooResponse get(String uri) {
        return new MangooResponse(uri, Methods.GET);
    }
    
    public static MangooResponse post(String uri) {
        return new MangooResponse(uri, Methods.POST);
    }

    public static MangooResponse put(String uri) {
        return new MangooResponse(uri, Methods.PUT);
    }
    
    public static MangooResponse delete(String uri) {
        return new MangooResponse(uri, Methods.DELETE);
    }
    
    public static MangooClient instance() {
        return new MangooClient();
    }
}