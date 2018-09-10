package io.mangoo.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import controllers.ApplicationController;
import io.mangoo.TestExtension;
import io.mangoo.enums.RouteType;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class RouterTest {
    
    @Test
    public void testAddRoute() {
        //given
        Router.addRoute(new Route(RouteType.REQUEST).toUrl("/foo").withMethod("bar").withClass(ApplicationController.class));
        
        //then
        assertThat(Router.getRoutes(), not(nullValue()));
        assertThat(Router.getRoutes().size(), greaterThan(0));
    }
    
    @Test()
    public void testMaxRoutes() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            for (int i=0; i <= 100000; i++) {
                Router.addRoute(new Route(RouteType.REQUEST).toUrl("/foo").withMethod("bar").withClass(ApplicationController.class));  
            }
          });
    }
}