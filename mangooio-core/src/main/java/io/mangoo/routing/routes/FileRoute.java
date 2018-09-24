package io.mangoo.routing.routes;

import java.util.Objects;

import io.mangoo.enums.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;

public class FileRoute implements MangooRoute {
    private String url;

    public String getUrl() {
        return this.url;
    }

    public void to(String url) {
        Objects.requireNonNull(url, Required.URL.toString());
        this.url = url;
                
        Router.addRoute(this);        
    }
}