package io.mangoo.filters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.admin.AdminFilter;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Template;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class AdminFilterTest {
    
    @Test
    public void testAdminNotEnabled() {
        //given
    		Config mockedConfig = Mockito.mock(Config.class);
    		Request mockedRequest = Mockito.mock(Request.class);
    		Response mockedResponse = Mockito.mock(Response.class);
      	AdminFilter filter = new AdminFilter(mockedConfig);
    		
    		//when
    		when(mockedConfig.isApplicationAdminEnable()).thenReturn(false);
    		Response response = filter.execute(mockedRequest, mockedResponse);
    	
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
        assertThat(response.getBody(), equalTo(Template.DEFAULT.notFound()));
        assertThat(response.isEndResponse(), equalTo(true));
    }
    
    @Test
    public void testAdminEnabledNotAuthorized() {
        //given
    		Config mockedConfig = Mockito.mock(Config.class);
    		Request mockedRequest = Mockito.mock(Request.class);
    		Response mockedResponse = Mockito.mock(Response.class);
      	AdminFilter filter = new AdminFilter(mockedConfig);
    		
    		//when
      	when(mockedConfig.isApplicationAdminEnable()).thenReturn(true);
    		when(mockedRequest.getHeader(Header.AUTHORIZATION.toHttpString())).thenReturn("");
    		Response response = filter.execute(mockedRequest, mockedResponse);
    	
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getHeader(Header.WWW_AUTHENTICATE.toHttpString()), equalTo("Basic realm=Administration authentication"));
        assertThat(response.getBody(), equalTo(""));
        assertThat(response.isEndResponse(), equalTo(true));
    }
    
    @Test
    public void testAdminEnabledAuthorized() {
        //given
    		Config config = Application.getInstance(Config.class);
    		Request mockedRequest = Mockito.mock(Request.class);
    		Response mockedResponse = Mockito.mock(Response.class);
      	AdminFilter filter = new AdminFilter(config);
    		
    		//when
    		when(mockedRequest.getHeader(Header.AUTHORIZATION.toHttpString())).thenReturn("Basic YWRtaW46YWRtaW4=");
    		Response response = filter.execute(mockedRequest, mockedResponse);
    	
        //then
        assertThat(response, not(nullValue()));
        assertThat(response, equalTo(mockedResponse));
    }
}