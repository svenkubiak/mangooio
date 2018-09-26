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
public class ServerSentEventRoute implements MangooRoute {
    private String url;
    private boolean authentication;

    /**
     * Sets the URL for this route
     * 
     * @param url The URL for this route
     * @return ServerSentEventRoute instance
     */
    public ServerSentEventRoute to(String url) {
        Objects.requireNonNull(url, Required.URL.toString());

        if ('/' != url.charAt(0)) {
            url = "/" + url;
        }
        this.url = url;
        
        Router.addRoute(this);  
        
        return this;
    }
    
    /**
     * Sets authentication to true, default is false
     */
    public void requireAuthentication() {
        this.authentication = true;
    }
    
    @Override
    public String getUrl() {
        return this.url;
    }
    
    public boolean hasAuthentication() {
        return this.authentication;
    }
}