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
//    private static final String TEXT_HTML = "text/html; charset=UTF-8";
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";
//    private static final String SCHEDULER = "scheduler";
//    private static final String METRICS = "metrics";
//    private static final String CACHE = "cache";
//    private static final String ROUTES = "routes";
//    private static final String CONFIG = "config";
    private static final String ALIVE = "alive";
    private static final String ADMIN = "admin";
//    private static final String PROPERTIES = "properties";
//    private static final String MEMORY = "memory";    

    @Test
    public void testAdminAuthorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@admin")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), containsString(ALIVE));
    }
    
    @Test
    public void testAdminUnaothorized() {
        //given
        HTTPResponse response = HTTPRequest.get("/@admin").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(ALIVE)));
    }
}