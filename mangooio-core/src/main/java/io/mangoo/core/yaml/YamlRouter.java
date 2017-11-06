package io.mangoo.core.yaml;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author svenkubiak
 *
 */
public class YamlRouter {
    private List<YamlRoute> routes;

    public List<YamlRoute> getRoutes() {
        return new ArrayList<>(routes);
    }

    public void setRoutes(List<YamlRoute> routes) {
        this.routes = new ArrayList<>(routes);
    }
}