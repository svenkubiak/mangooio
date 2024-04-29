package io.mangoo.controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class AdminControllerTest {
    private static final String TEXT_HTML = "text/html; charset=UTF-8";
    private static final String EVICTIONS = "Evictions";
    private static final String LOGGER = "logger";
    private static final String SCHEDULER = "scheduler";
    private static final String TOOLS = "tools";
    private static final String CACHE = "cache";
    private static final String CONTROL_PANEL = "Dashboard";
    
    @Test
    void testDashboardUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(CONTROL_PANEL)));
    }
    
    @Test
    void testCacheAuthorized() {
        //given
        TestResponse response = login().to("/@admin/cache")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CACHE));
        assertThat(response.getContent(), containsString(EVICTIONS));
    }

    @Test
    void testCacheUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/cache")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(CONTROL_PANEL)));
    }
    
    @Test
    void testDashboardAuthorized() {
        //given
        TestResponse response = login().to("/@admin")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CONTROL_PANEL));
    }
    
    @Test
    void testLoggerUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/logger")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(LOGGER)));
    }
    
    @Test
    void testLoggerAuthorized() {
        //given
        TestResponse response = login().to("/@admin/logger")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(LOGGER));
    }
    
    @Test
    void testToolsAuthorized() {
        //given
        TestResponse response = login().to("/@admin/tools")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(TOOLS));
    }
    
    @Test
    void testToolsTwoFactorAuthorized() {
        //given
        TestResponse response = login().to("/@admin/tools")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("Two Factor Authentication"));
    }
    
    @Test
    void testToolsUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/tools")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(TOOLS)));
    }
    
    @Test
    void testToolsAjaxAuthorized() {
        //given
        TestResponse response = login().to("/@admin/tools/ajax")
                .withHTTPMethod(Methods.POST.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("application/json; charset=UTF-8"));
    }
    
    @Test
    void testToolsAjaxUnauthorized() {
        //given
        TestResponse response = TestRequest.post("/@admin/tools/ajax")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    void testLoggerAjaxAuthorized() {
        //given
        TestResponse response = login().to("/@admin/logger/ajax")
                .withHTTPMethod(Methods.POST.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("text/plain; charset=UTF-8"));
    }
    
    @Test
    void testLoggerAjaxUnauthorized() {
        //given
        TestResponse response = TestRequest.post("/@admin/logger/ajax")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    void testLogin() {
        //given
        TestResponse response = TestRequest.get("/@admin/login").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("login"));
    }
    
    private TestResponse login() {
        Multimap<String, String> parameters = ArrayListMultimap.create();
        parameters.put("username", Application.getInstance(Config.class).getApplicationAdminUsername());
        parameters.put("password", Application.getInstance(Config.class).getApplicationAdminPassword());
        TestResponse response = TestRequest.post("/@admin/authenticate")
                .withForm(parameters)
                .execute();
        
        return response;
    }
}