package io.mangoo.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Test;

import io.mangoo.routing.routes.Route;

/**
 * 
 * @author svenkubiak
 *
 */
public class RoutingTest {
    
    @Test
    public void testAddResourcePath() {
        //given
        Route route = Routing.ofResourcePath().to("woohoo");
        
        //then
        assertThat(route.getUrl(), startsWith("/"));
        assertThat(route.getUrl(), endsWith("/"));
    }
    
    @Test
    public void testAddResourceFile() {
        //given
        Route route = Routing.ofResourceFile().to("/woohoo.txt");
        
        //then
        assertThat(route.getUrl(), startsWith("/"));
    }
    
    @Test
    public void testAddRoute() {
        //given
        int oldSize = Routing.getRoutes().size();
        Routing.ofResourceFile().to("/woohoo.txt").add();
        
        //then
        assertThat(Routing.getRoutes(), notNullValue());
        assertThat(Routing.getRoutes().size(), equalTo(oldSize + 1));
    }
}