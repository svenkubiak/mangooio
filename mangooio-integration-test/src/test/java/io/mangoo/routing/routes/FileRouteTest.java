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
public class FileRouteTest {

    @Test
    public void testTo() {
        //given
        FileRoute fileRoute = new FileRoute();
        
        //when
        fileRoute.to("foo");
        
        //then
        assertThat(fileRoute.getUrl(), equalTo("/foo"));
        
        //when
        fileRoute.to("/bar");
        
        //then
        assertThat(fileRoute.getUrl(), equalTo("/bar"));
    }
    
    @Test
    public void testAddingRoute() {
        //given
        String url = "/" + UUID.randomUUID().toString();
        FileRoute fileRoute = new FileRoute();
        
        //when
        fileRoute.to(url);
        
        //then
        Set<FileRoute> fileRoutes = Router.getRoutes().stream()
                .filter(FileRoute.class::isInstance)
                .map(FileRoute.class::cast)
                .collect(Collectors.toSet());
        
        Set<FileRoute> collectedRoutes = fileRoutes.stream()
                .filter(r -> r.getUrl().equals(url))
                .collect(Collectors.toSet());
        
        assertThat(collectedRoutes.size(), equalTo(1));
    }
}
