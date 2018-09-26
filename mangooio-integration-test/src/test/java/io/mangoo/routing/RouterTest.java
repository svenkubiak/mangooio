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
import io.mangoo.core.Application;
import io.mangoo.interfaces.MangooBootstrap;

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
        On.get().to("/foo").respondeWith("index");
        
        //then
        assertThat(Router.getRoutes(), not(nullValue()));
        assertThat(Router.getRoutes().size(), greaterThan(0));
    }
    
    @Test()
    public void testMaxRoutes() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            for (int i=0; i <= 100000; i++) {
                Bind.controller(ApplicationController.class).withRoutes(
                        On.get().to("/foo").respondeWith("index")
                );
            }
          });
        
        Router.reset();
        Application.getInstance(MangooBootstrap.class).initializeRoutes();
    }
}