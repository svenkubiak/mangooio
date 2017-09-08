package io.mangoo.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.net.MediaType;

import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.oauth.OAuthProvider;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public class RequestHelperTest {

	@Test
	public void testGetRequestParameters() {
        //given
		Deque<String> paramsOne = new LinkedList<>();
		paramsOne.push("foo");
		Deque<String> paramsTwo = new LinkedList<>();
		paramsTwo.push("bar");
		
		Map<String, Deque<String>> queryParameters = new HashMap<>();
		Map<String, Deque<String>> pathParameters = new HashMap<>();
		queryParameters.put("first", paramsOne);
		pathParameters.put("second", paramsTwo);
		
		HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		
    		//when
    		when(mockedExchange.getQueryParameters()).thenReturn(queryParameters);
    		when(mockedExchange.getPathParameters()).thenReturn(pathParameters);
    		Map<String, String> requestParameters = helper.getRequestParameters(mockedExchange);
    	
        //then
        assertThat(requestParameters, not(nullValue()));
        assertThat(requestParameters.get("first"), equalTo("foo"));
        assertThat(requestParameters.get("second"), equalTo("bar"));
	}
	
	@Test
	public void testIsPost() {
        //given
		HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		
    		//when
    		when(mockedExchange.getRequestMethod()).thenReturn(Methods.POST);
    		boolean postPutPatch = helper.isPostPutPatch(mockedExchange);
    	
        //then
        assertThat(postPutPatch, equalTo(true));
	}
	
	@Test
	public void testIsPut() {
        //given
		HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		
    		//when
    		when(mockedExchange.getRequestMethod()).thenReturn(Methods.PUT);
    		boolean postPutPatch = helper.isPostPutPatch(mockedExchange);
    	
        //then
        assertThat(postPutPatch, equalTo(true));
	}
	
	@Test
	public void testIsPatch() {
        //given
		HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		
    		//when
    		when(mockedExchange.getRequestMethod()).thenReturn(Methods.PUT);
    		boolean postPutPatch = helper.isPostPutPatch(mockedExchange);
    	
        //then
        assertThat(postPutPatch, equalTo(true));
	}
	
	@Test
	public void testIsNonPostPutPatch() {
        //given
		HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		
    		//when
    		when(mockedExchange.getRequestMethod()).thenReturn(Methods.GET);
    		boolean postPutPatch = helper.isPostPutPatch(mockedExchange);
    	
        //then
        assertThat(postPutPatch, equalTo(false));
	}
	
	@Test
	public void testIsJsonRequest() {
        //given
		HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		HeaderMap headerMap = new HeaderMap();
    		headerMap.put(Header.CONTENT_TYPE.toHttpString(), MediaType.JSON_UTF_8.withoutParameters().toString());
    		
    		//when
    		when(mockedExchange.getRequestHeaders()).thenReturn(headerMap);
    		boolean isJson = helper.isJsonRequest(mockedExchange);
    	
        //then
        assertThat(isJson, equalTo(true));
	}
	
	@Test
	public void testIsNonJsonRequest() {
        //given
		HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		HeaderMap headerMap = new HeaderMap();
    		headerMap.put(Header.CONTENT_TYPE.toHttpString(), MediaType.CSS_UTF_8.withoutParameters().toString());
    		
    		//when
    		when(mockedExchange.getRequestHeaders()).thenReturn(headerMap);
    		boolean isJson = helper.isJsonRequest(mockedExchange);
    	
        //then
        assertThat(isJson, equalTo(false));
	}
	
	@Test
	public void testWrapSecurity() {
        //given
		HttpHandler handler = Mockito.mock(HttpHandler.class);
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		
    		//when
    		HttpHandler security = helper.wrapSecurity(handler, "foo", "bar");
    		
        //then
        assertThat(security, not(equalTo(null)));
        assertThat(security.getClass().getSimpleName(), equalTo("SecurityInitialHandler"));
	}
	
	@Test
	public void testGetOAuthProvider() {
        //given
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		
    		//when
    		Optional<OAuthProvider> twitter = helper.getOAuthProvider("twitter");
    		Optional<OAuthProvider> facebook = helper.getOAuthProvider("facebook");
    		Optional<OAuthProvider> google = helper.getOAuthProvider("google");
    	
        //then
        assertThat(twitter, not(nullValue()));
        assertThat(twitter.isPresent(), equalTo(true));
        assertThat(twitter.get().name(), equalTo("TWITTER"));
        assertThat(facebook, not(nullValue()));
        assertThat(facebook.isPresent(), equalTo(true));
        assertThat(facebook.get().name(), equalTo("FACEBOOK"));
        assertThat(google, not(nullValue()));
        assertThat(google.isPresent(), equalTo(true));
        assertThat(google.get().name(), equalTo("GOOGLE"));
	}
}