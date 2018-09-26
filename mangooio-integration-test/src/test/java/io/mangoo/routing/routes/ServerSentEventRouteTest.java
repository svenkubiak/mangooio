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
public class ServerSentEventRouteTest {
    
    @Test
    public void testTo() {
        //given
        ServerSentEventRoute serverSentEventRoute = new ServerSentEventRoute();
        
        //when
        serverSentEventRoute.to("foo");
        
        //then
        assertThat(serverSentEventRoute.getUrl(), equalTo("/foo"));
        
        //when
        serverSentEventRoute.to("/bar");
        
        //then
        assertThat(serverSentEventRoute.getUrl(), equalTo("/bar"));
    }
    
    @Test
    public void testAddingRoute() {
        //given
        String url = "/" + UUID.randomUUID().toString();
        ServerSentEventRoute serverSentEventRoute = new ServerSentEventRoute();
        
        //when
        serverSentEventRoute.to(url);
        
        //then
        Set<ServerSentEventRoute> serverSentEventRoutes = Router.getRoutes().stream()
                .filter(ServerSentEventRoute.class::isInstance)
                .map(ServerSentEventRoute.class::cast)
                .collect(Collectors.toSet());
        
        Set<ServerSentEventRoute> collectedRoutes = serverSentEventRoutes.stream()
                .filter(r -> r.getUrl().equals(url))
                .collect(Collectors.toSet());
        
        assertThat(collectedRoutes.size(), equalTo(1));
    }
    
    @Test
    public void testWithAuthentication() {
        //given
        String url = "/" + UUID.randomUUID().toString();
        ServerSentEventRoute serverSentEventRoute = new ServerSentEventRoute();
        
        //when
        serverSentEventRoute.to(url).requireAuthentication();
        
        //then
        Set<ServerSentEventRoute> serverSentEventRoutes = Router.getRoutes().stream()
                .filter(ServerSentEventRoute.class::isInstance)
                .map(ServerSentEventRoute.class::cast)
                .collect(Collectors.toSet());
        
        Set<ServerSentEventRoute> collectedRoutes = serverSentEventRoutes.stream()
                .filter(r -> r.getUrl().equals(url))
                .collect(Collectors.toSet());
        
        assertThat(collectedRoutes.size(), equalTo(1));
        assertThat(collectedRoutes.iterator().next().hasAuthentication(), equalTo(true));
    }
}
