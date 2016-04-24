package io.mangoo.utils.http;

/**
 * 
 * @author svenkubiak
 *
 */
public class HTTPBrowser extends HTTPResponse {
    public static HTTPBrowser open() {
        return new HTTPBrowser();
    }
}