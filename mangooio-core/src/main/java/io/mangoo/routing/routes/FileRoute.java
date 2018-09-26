package io.mangoo.routing.routes;

import java.util.Objects;

import io.mangoo.enums.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;

/**
 * 
 * @author svenkubiak
 *
 */
public class FileRoute implements MangooRoute {
    private String url;

    /**
     * Sets the URL for this route
     * 
     * @param url The URL for this route
     */
    public void to(String url) {
        Objects.requireNonNull(url, Required.URL.toString());
        
        if ('/' != url.charAt(0)) {
            url = "/" + url;
        }
        
        this.url = url;
        
        Router.addRoute(this);        
    }

    @Override
    public String getUrl() {
        return this.url;
    }
}