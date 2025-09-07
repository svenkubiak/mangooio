package io.mangoo.routing.routes;

import io.mangoo.constants.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;

import java.util.Objects;

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
        Objects.requireNonNull(url, Required.URL);

        if ('/' != url.charAt(0)) {
            url = "/" + url;
        }
        this.url = url;
        
        Router.addRoute(this, "sse");
        
        return this;
    }
    
    /**
     * Sets authentication to true, default is false
     */
    public void withAuthentication() {
        authentication = true;
    }
    
    @Override
    public String getUrl() {
        return url;
    }
    
    public boolean hasAuthentication() {
        return authentication;
    }
}