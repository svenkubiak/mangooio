package io.mangoo.utils.http;

/**
 * 
 * @author svenkubiak
 *
 */
public class WebBrowser extends WebResponse {
    public static WebBrowser open() {
        return new WebBrowser();
    }
}