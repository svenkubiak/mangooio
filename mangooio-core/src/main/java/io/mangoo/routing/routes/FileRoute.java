package io.mangoo.routing.routes;

import io.mangoo.constants.NotNull;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;

import java.util.Objects;

public class FileRoute implements MangooRoute {
    private String url;

    /**
     * Sets the URL for this route
     * 
     * @param url The URL for this route
     */
    public void to(String url) {
        Objects.requireNonNull(url, NotNull.URL);
        
        if ('/' != url.charAt(0)) {
            url = "/" + url;
        }
        
        this.url = url;
        
        Router.addRoute(this);        
    }

    @Override
    public String getUrl() {
        return url;
    }
}