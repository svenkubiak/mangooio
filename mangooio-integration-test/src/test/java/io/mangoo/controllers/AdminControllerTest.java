package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.utils.http.HTTPRequest;
import io.mangoo.utils.http.HTTPResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class AdminControllerTest {
    private static final String TEXT_HTML = "text/html; charset=UTF-8";
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";
    private static final String SCHEDULER = "scheduler";
    private static final String METRICS = "metrics";
    private static final String CACHE = "cache";
    private static final String ROUTES = "routes";
    private static final String CONFIG = "config";
    private static final String ALIVE = "alive";
    private static final String ADMIN = "admin";
    private static final String PROPERTIES = "properties";
    private static final String MEMORY = "memory";    

    @Test
    public void testHealthAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@health")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), containsString(ALIVE));
    }
    
    @Test
    public void testHealthUnaothorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@health").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(ALIVE)));
    }

    @Test
    public void testConfigAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@config")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CONFIG));
    }
    
    @Test
    public void testConfigUnauthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@config").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(CONFIG)));
    }

    @Test
    public void testRoutedAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@routes")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(ROUTES));
    }
    
    @Test
    public void testRoutedUnauthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@routes").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(ROUTES)));
    }

    @Test
    public void testCacheAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@cache")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CACHE));
    }
    
    @Test
    public void testCacheUnauthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@cache").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(CACHE)));
    }

    @Test
    public void testMetricsAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@metrics")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(METRICS));
    }
    
    @Test
    public void testMetricsUnauthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@metrics").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(METRICS)));
    }
    
    @Test
    public void testSchedulerAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@scheduler")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(SCHEDULER));
    }
    
    @Test
    public void testSchedulerUnAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@scheduler").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testSystemAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@system")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(PROPERTIES));
    }
    
    @Test
    public void testSystemUnAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@system").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(PROPERTIES)));
    }
    
    @Test
    public void testMemoryAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@memory")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(MEMORY));
    }
    
    @Test
    public void testMemoryUnAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@memory").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(MEMORY)));
    }
}