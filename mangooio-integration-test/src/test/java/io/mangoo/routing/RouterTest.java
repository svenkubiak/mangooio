package io.mangoo.routing;

import controllers.ApplicationController;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.enums.Http;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.routing.routes.RequestRoute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class RouterTest {
    
    @Test
    void testAddRoute() {
        //given
        On.get().to("/foo").respondeWith("index");
        
        //then
        assertThat(Router.getRoutes(), not(nullValue()));
        assertThat(Router.getRoutes().size(), greaterThan(0));
    }
    
    @Test
    void testAnyOfRegistersEveryMethod() {
        //given
        Bind.controller(ApplicationController.class).withRoutes(
                On.anyOf(Http.GET, Http.POST).to("/anyof-router").respondeWith("index")
        );

        //when
        List<RequestRoute> routes = Router.getRequestRoutes()
                .filter(route -> "/anyof-router".equals(route.getUrl()))
                .toList();

        //then
        assertThat(routes.stream().map(RequestRoute::getMethod).toList(), containsInAnyOrder(Http.GET, Http.POST));
        assertThat(routes.stream().map(RequestRoute::getControllerClass).toList(), everyItem(equalTo(ApplicationController.class)));
    }

    @Test()
    void testMaxRoutes() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            for (int i=0; i <= 100000; i++) {
                Bind.controller(ApplicationController.class).withRoutes(
                        On.get().to("/foo").respondeWith("index")
                );
            }
          }, "Failed to add max number of routes");
        
        Router.reset();
        Application.getInstance(MangooBootstrap.class).initializeRoutes();
    }
}