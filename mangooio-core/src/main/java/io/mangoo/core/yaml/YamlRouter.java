package io.mangoo.core.yaml;

import java.util.ArrayList;
import java.util.List;

import io.mangoo.core.yaml.YamlRoute;

/**
 * 
 * @author sven.kubiak
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