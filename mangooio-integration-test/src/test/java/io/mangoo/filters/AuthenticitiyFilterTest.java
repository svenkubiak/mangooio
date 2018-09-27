package io.mangoo.filters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.enums.Template;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class AuthenticitiyFilterTest {
    @Test
    public void testAuthenticityMatch() {
        //given
            AuthenticityFilter filter = Application.getInstance(AuthenticityFilter.class);
            Request mockedRequest = Mockito.mock(Request.class);
            Response mockedResponse = Mockito.mock(Response.class);
            
            //when
            when(mockedRequest.authenticityMatches()).thenReturn(true);
            Response response = filter.execute(mockedRequest, mockedResponse);
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(mockedResponse, equalTo(response));
    }
    
    @Test
    public void testAuthenticityNotMatch() {
        //given
            AuthenticityFilter filter = Application.getInstance(AuthenticityFilter.class);
            Request mockedRequest = Mockito.mock(Request.class);
            Response mockedResponse = Mockito.mock(Response.class);
            
            //when
            when(mockedRequest.authenticityMatches()).thenReturn(false);
            Response response = filter.execute(mockedRequest, mockedResponse);
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
        assertThat(response.getBody(), equalTo(Template.DEFAULT.forbidden()));
        assertThat(response.isEndResponse(), equalTo(true));
    }
}