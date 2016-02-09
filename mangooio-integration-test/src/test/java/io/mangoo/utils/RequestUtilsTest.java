package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.enums.oauth.OAuthProvider;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.websockets.core.WebSocketChannel;

/**
 * 
 * @author svenkubiak
 *
 */
public class RequestUtilsTest {
    
    @Test
    public void testGetOAuthProvider() {
        //given
        OAuthProvider twitter = RequestUtils.getOAuthProvider("twitter");
        OAuthProvider google = RequestUtils.getOAuthProvider("google");
        OAuthProvider facebook = RequestUtils.getOAuthProvider("facebook");
        
        //then
        assertThat(twitter, equalTo(OAuthProvider.TWITTER));
        assertThat(google, equalTo(OAuthProvider.GOOGLE));
        assertThat(facebook, equalTo(OAuthProvider.FACEBOOK));
    }
    
    @Test
    public void testGetServerSentEventURL() {
        //given
    	final ServerSentEventConnection connection = Mockito.mock(ServerSentEventConnection.class);
    	when(connection.getRequestURI()).thenReturn("abc://username:password@example.com:123/path/data?key=value#fragid1");
        
    	//when
    	String url = RequestUtils.getServerSentEventURL(connection);
    	
        //then
        assertThat(url, equalTo("/path/data?key=value#fragid1"));
    }
    
    @Test
    public void testGetWebSocketURL() {
        //given
    	final WebSocketChannel channel = Mockito.mock(WebSocketChannel.class);
    	when(channel.getUrl()).thenReturn("abc://username:password@example.com:123/path/data?key=value#fragid1");
        
    	//when
    	String url = RequestUtils.getWebSocketURL(channel);
    	
        //then
        assertThat(url, equalTo("/path/data?key=value#fragid1"));
    }
}