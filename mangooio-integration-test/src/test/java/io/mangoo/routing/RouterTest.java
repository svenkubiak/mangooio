package io.mangoo.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import controllers.ApplicationController;
import io.mangoo.enums.RouteType;

/**
 * 
 * @author svenkubiak
 *
 */
public class RouterTest {
    
    @Test
    public void testAddRoute() {
        //given
        Router.addRoute(new Route(RouteType.REQUEST).toUrl("/foo").withMethod("bar").withClass(ApplicationController.class));
        
        //then
        assertThat(Router.getRoutes(), not(nullValue()));
        assertThat(Router.getRoutes().size(), greaterThan(0));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMaxRoutes() {
        //given
        for (int i=0; i <= 100000; i++) {
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/foo").withMethod("bar").withClass(ApplicationController.class));  
        }
    }
}