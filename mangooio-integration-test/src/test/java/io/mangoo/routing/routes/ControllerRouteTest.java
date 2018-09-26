package io.mangoo.routing.routes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import controllers.ApplicationController;
import io.mangoo.TestExtension;
import io.mangoo.routing.On;
import io.mangoo.routing.Router;

@ExtendWith({TestExtension.class})
public class ControllerRouteTest {

    @Test
    public void testCreation() {
        //given
        Class<?> clazz = ApplicationController.class;
        
        //when
        ControllerRoute route = new ControllerRoute(clazz);
        
        //then
        assertThat(route.getControllerClass(), equalTo(clazz));
    }
    
    @Test
    public void testWithMethods() {
        //given
        String route1 = UUID.randomUUID().toString();
        String route2 = UUID.randomUUID().toString();
        
        //when
        ControllerRoute route = new ControllerRoute(ApplicationController.class);
        route.withRoutes(
                On.get().to("/route1").respondeWith(route1),
                On.post().to("/route2").respondeWith(route2)
        );
        
        //then
        Set<RequestRoute> requestRoutes = Router.getRoutes().stream()
            .filter(RequestRoute.class::isInstance)
            .map(RequestRoute.class::cast)
            .collect(Collectors.toSet());
        
        Set<RequestRoute> collectedRoutes = requestRoutes.stream()
            .filter(r -> r.getControllerMethod().equals(route1) || r.getControllerMethod().equals(route2))
            .collect(Collectors.toSet());
        
        assertThat(collectedRoutes.size(), equalTo(2));
    }
    
    @Test
    public void testWithBasicAuthentication() {
        //given
        String route1 = UUID.randomUUID().toString();
        
        //when
        ControllerRoute route = new ControllerRoute(ApplicationController.class);
        route.withBasicAuthentication("foo", "bar").withRoutes(
                On.get().to("/route1").respondeWith(route1)
        );
        
        //then
        Set<RequestRoute> requestRoutes = Router.getRoutes().stream()
                .filter(RequestRoute.class::isInstance)
                .map(RequestRoute.class::cast)
                .collect(Collectors.toSet());
        
        Set<RequestRoute> collectedRoutes = requestRoutes.stream()
                .filter(r -> r.getControllerMethod().equals(route1))
                .collect(Collectors.toSet());
        
        RequestRoute requestRoute = collectedRoutes.iterator().next();
        
        assertThat(requestRoute.getUsername(), equalTo("foo"));
        assertThat(requestRoute.getPassword(), equalTo("bar"));
    }
}