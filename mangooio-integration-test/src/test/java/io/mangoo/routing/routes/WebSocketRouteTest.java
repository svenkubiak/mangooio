package io.mangoo.routing.routes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import controllers.WebSocketController;
import io.mangoo.TestExtension;
import io.mangoo.routing.Router;

@ExtendWith({TestExtension.class})
public class WebSocketRouteTest {
    
    @Test
    public void testTo() {
        //given
        WebSocketRoute webSocketRoute = new WebSocketRoute();
        
        //when
        webSocketRoute.to("foo");
        
        //then
        assertThat(webSocketRoute.getUrl(), equalTo("/foo"));
        
        //when
        webSocketRoute.to("/bar");
        
        //then
        assertThat(webSocketRoute.getUrl(), equalTo("/bar"));
    }
    
    @Test
    public void testWithController() {
        //given
        WebSocketRoute webSocketRoute = new WebSocketRoute();
        
        //when
        webSocketRoute.onController(WebSocketController.class);
        
        //then
        assertThat(webSocketRoute.getControllerClass(), equalTo(WebSocketController.class));
    }
    
    @Test
    public void testAddingRoute() {
        //given
        String url = "/" + UUID.randomUUID().toString();
        WebSocketRoute webSocketRoute = new WebSocketRoute();
        
        //when
        webSocketRoute.to(url);
        
        //then
        Set<WebSocketRoute> webSocketRoutes = Router.getRoutes().stream()
                .filter(WebSocketRoute.class::isInstance)
                .map(WebSocketRoute.class::cast)
                .collect(Collectors.toSet());
        
        Set<WebSocketRoute> collectedRoutes = webSocketRoutes.stream()
                .filter(r -> r.getUrl().equals(url))
                .collect(Collectors.toSet());
        
        assertThat(collectedRoutes.size(), equalTo(1));
    }
    
    @Test
    public void testWithAuthentication() {
        //given
        String url = "/" + UUID.randomUUID().toString();
        WebSocketRoute webSocketRoute = new WebSocketRoute();
        
        //when
        webSocketRoute.to(url).requireAuthentication();
        
        //then
        Set<WebSocketRoute> webSocketRoutes = Router.getRoutes().stream()
                .filter(WebSocketRoute.class::isInstance)
                .map(WebSocketRoute.class::cast)
                .collect(Collectors.toSet());
        
        Set<WebSocketRoute> collectedRoutes = webSocketRoutes.stream()
                .filter(r -> r.getUrl().equals(url))
                .collect(Collectors.toSet());
        
        assertThat(collectedRoutes.size(), equalTo(1));
        assertThat(collectedRoutes.iterator().next().hasAuthentication(), equalTo(true));
    }
}
