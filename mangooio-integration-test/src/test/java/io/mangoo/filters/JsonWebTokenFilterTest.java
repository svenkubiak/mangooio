package io.mangoo.filters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class JsonWebTokenFilterTest {
    private static final String VALID_BEARER = "zcACe7mxq6DPTK1XXpoekqp2yNirjhXso+nkzsCnMw5r9r6Dg3oiabHsQDt7l2KsfxqSpYIH2FJ/Zl5Mkh3H7Z3LC1FvfIyNhL1jsbQq70XeTdcQVKO/HH4Evjv7daU9UARBVKVTgGei6Cbw8EAy+FmrkCbgUnk5jtKpo3pWkgU=";
	
    @Test
    public void testNoSignKeyAndNoToken() {
        //given
		Request mockedRequest = Mockito.mock(Request.class);
		Response mockedResponse = Mockito.mock(Response.class);
    		Config mockedConfig = Mockito.mock(Config.class);
    		JsonWebTokenFilter filter = new JsonWebTokenFilter(mockedConfig);
    		
    		//when
    		when(mockedRequest.getHeader(Header.AUTHORIZATION.toHttpString())).thenReturn("");
    		when(mockedConfig.getJwtsSignKey()).thenReturn("");
    		Response response = filter.execute(mockedRequest, mockedResponse);
    	
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
        assertThat(response.getBody(), equalTo(""));
        assertThat(response.isEndResponse(), equalTo(true));
    }
    
    @Test
    public void testInvalidToken() {
        //given
		Request mockedRequest = Mockito.mock(Request.class);
		Response mockedResponse = Mockito.mock(Response.class);
    		Config mockedConfig = Mockito.mock(Config.class);
    		JsonWebTokenFilter filter = new JsonWebTokenFilter(mockedConfig);
    		String bearer = "Bearer fjkfdsjklnjvkdsbn";
    		Config config = Application.getInstance(Config.class);
    		
    		//when
    		when(mockedRequest.getHeader(Header.AUTHORIZATION.toHttpString())).thenReturn(bearer);
    		when(mockedConfig.getJwtsSignKey()).thenReturn(config.getJwtsSignKey());
    		Response response = filter.execute(mockedRequest, mockedResponse);
    	
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getBody(), equalTo(""));
        assertThat(response.isEndResponse(), equalTo(true));
    }
    
    @Test
    public void testValidToken() {
        //given
		Request mockedRequest = Mockito.mock(Request.class);
		Response mockedResponse = Mockito.mock(Response.class);
    		Config mockedConfig = Mockito.mock(Config.class);
    		JsonWebTokenFilter filter = new JsonWebTokenFilter(mockedConfig);
    		Config config = Application.getInstance(Config.class);
    		
    		//when
    		when(mockedConfig.getJwtsEncryptionKey()).thenReturn(config.getJwtsEncryptionKey());
    		when(mockedConfig.isJwtsEncrypted()).thenReturn(true);
    		when(mockedRequest.getHeader(Header.AUTHORIZATION.toHttpString())).thenReturn(VALID_BEARER);
    		when(mockedConfig.getJwtsSignKey()).thenReturn(config.getJwtsSignKey());
    		Response response = filter.execute(mockedRequest, mockedResponse);
    		
        //then
        assertThat(response, not(nullValue()));
        assertThat(response, equalTo(mockedResponse));
    }
}