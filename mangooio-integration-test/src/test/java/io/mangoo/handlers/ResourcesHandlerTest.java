package io.mangoo.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class ResourcesHandlerTest {
    
    @Test
    void testResourceFile() {
        //given
        TestResponse response = TestRequest.get("/robots.txt").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo("text/plain"));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    void testResourcePathJavaScript() {
        //given
        TestResponse response = TestRequest.get("/assets/javascript/jquery.min.js").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo("application/javascript"));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    void testResourcePathStylesheet() {
        //given
        TestResponse response = TestRequest.get("/assets/stylesheet/css.css").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo("text/css"));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
}