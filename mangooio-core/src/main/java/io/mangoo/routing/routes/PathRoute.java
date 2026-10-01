package io.mangoo.routing.routes;

import io.mangoo.constants.Required;
import io.mangoo.interfaces.MangooRoute;
import io.mangoo.routing.Router;

import java.util.Objects;

public class PathRoute implements MangooRoute {
    private String url;

    public void to(String url) {
        Objects.requireNonNull(url, Required.URL);

        if ('/' != url.charAt(0)) {
            url = "/" + url;
        }

        if (!url.endsWith("/")) {
            url = url + "/";
        }
        
        this.url = url;
        
        Router.addRoute(this, "path");
    }

    @Override
    public String getUrl() {
        return url;
    }
}