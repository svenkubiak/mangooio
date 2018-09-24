package io.mangoo.routing.routes;

import java.util.Objects;

import io.mangoo.enums.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;

public class WebSocketRoute implements MangooRoute {
    private String url;
    private Class<?> controller;
    
    public String getUrl() {
        return url;
    }

    public Class<?> getControllerClass() {
        return controller;
    }

    public WebSocketRoute to(String url) {
        Objects.requireNonNull(url, Required.URL.toString());
        this.url = url;
        
        Router.addRoute(this);  
        
        return this;
    }
    
    public WebSocketRoute onController(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.URL.toString());
        
        this.controller = clazz;
        return this;
    }
}