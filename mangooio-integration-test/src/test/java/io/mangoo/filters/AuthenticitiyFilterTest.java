package io.mangoo.filters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Key;
import io.mangoo.enums.Template;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthenticitiyFilterTest {
    
    @Test
    public void testInvalidAuthenticationWithoutRedirect() {
        //given
		Request mockedRequest = Mockito.mock(Request.class);
		Response mockedResponse = Mockito.mock(Response.class);
		Authentication mockedAuthentication = Mockito.mock(Authentication.class);
    		Config mockedConfig = Mockito.mock(Config.class);
    		AuthenticationFilter filter = new AuthenticationFilter(mockedConfig);
    		
    		//when
    		when(mockedRequest.getAuthentication()).thenReturn(mockedAuthentication);
    		when(mockedAuthentication.isValid()).thenReturn(false);
    		when(mockedConfig.getString(Key.AUTHENTICATION_REDIRECT.toString())).thenReturn(null);
    		Response response = filter.execute(mockedRequest, mockedResponse);
    	
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getBody(), equalTo(Template.DEFAULT.forbidden()));
        assertThat(response.isEndResponse(), equalTo(true));
    }
    
    @Test
    public void testInvalidAuthenticationWithRedirect() {
        //given
		Request mockedRequest = Mockito.mock(Request.class);
		Response mockedResponse = Mockito.mock(Response.class);
		Authentication mockedAuthentication = Mockito.mock(Authentication.class);
    		Config mockedConfig = Mockito.mock(Config.class);
    		AuthenticationFilter filter = new AuthenticationFilter(mockedConfig);
    		
    		//when
    		when(mockedRequest.getAuthentication()).thenReturn(mockedAuthentication);
    		when(mockedAuthentication.isValid()).thenReturn(false);
    		when(mockedConfig.getString(Key.AUTHENTICATION_REDIRECT.toString())).thenReturn("/login");
    		Response response = filter.execute(mockedRequest, mockedResponse);
    	
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getRedirectTo(), equalTo("/login"));
        assertThat(response.getBody(), equalTo(""));
    }
    
    @Test
    public void testValidAuthenticationWithoutTwoFactor() {
        //given
		Request mockedRequest = Mockito.mock(Request.class);
		Response mockedResponse = Mockito.mock(Response.class);
		Authentication mockedAuthentication = Mockito.mock(Authentication.class);
    		Config mockedConfig = Mockito.mock(Config.class);
    		AuthenticationFilter filter = new AuthenticationFilter(mockedConfig);
    		
    		//when
    		when(mockedRequest.getAuthentication()).thenReturn(mockedAuthentication);
    		when(mockedAuthentication.isValid()).thenReturn(true);
    		Response response = filter.execute(mockedRequest, mockedResponse);
    	
        //then
        assertThat(response, not(nullValue()));
        assertThat(response, equalTo(mockedResponse));
    }
    
    @Test
    public void testValidAuthenticationWithTwoFactor() {
        //given
		Request mockedRequest = Mockito.mock(Request.class);
		Response mockedResponse = Mockito.mock(Response.class);
		Authentication mockedAuthentication = Mockito.mock(Authentication.class);
    		Config mockedConfig = Mockito.mock(Config.class);
    		AuthenticationFilter filter = new AuthenticationFilter(mockedConfig);
    		
    		//when
    		when(mockedRequest.getAuthentication()).thenReturn(mockedAuthentication);
    		when(mockedAuthentication.isValid()).thenReturn(true);
    		when(mockedAuthentication.isTwoFactor()).thenReturn(true);
    		when(mockedConfig.getString(Key.AUTHENTICATION_REDIRECT.toString())).thenReturn("/login");
    		Response response = filter.execute(mockedRequest, mockedResponse);
    	
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getRedirectTo(), equalTo("/login"));
        assertThat(response.getBody(), equalTo(""));
    }
}