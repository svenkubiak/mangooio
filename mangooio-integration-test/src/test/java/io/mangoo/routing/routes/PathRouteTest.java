package io.mangoo.routing.routes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.routing.Router;

@ExtendWith({TestExtension.class})
public class PathRouteTest {
    
    @Test
    public void testTo() {
        //given
        PathRoute pathRoute = new PathRoute();
        
        //when
        pathRoute.to("foo");
        
        //then
        assertThat(pathRoute.getUrl(), equalTo("/foo/"));
        
        //when
        pathRoute.to("/bar/");
        
        //then
        assertThat(pathRoute.getUrl(), equalTo("/bar/"));
    }
    
    @Test
    public void testAddingRoute() {
        //given
        String url = "/" + UUID.randomUUID().toString() + "/";
        PathRoute pathRoute = new PathRoute();
        
        //when
        pathRoute.to(url);
        
        //then
        Set<PathRoute> pathRoutes = Router.getRoutes().stream()
                .filter(PathRoute.class::isInstance)
                .map(PathRoute.class::cast)
                .collect(Collectors.toSet());
        
        Set<PathRoute> collectedRoutes = pathRoutes.stream()
                .filter(r -> r.getUrl().equals(url))
                .collect(Collectors.toSet());
        
        assertThat(collectedRoutes.size(), equalTo(1));
    }
}