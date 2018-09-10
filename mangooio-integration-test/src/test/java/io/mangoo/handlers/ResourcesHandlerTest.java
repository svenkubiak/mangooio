package io.mangoo.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class ResourcesHandlerTest {
    
    @Test
    public void testResourceFile() {
        //given
        WebResponse response = WebRequest.get("/robots.txt").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo("text/plain"));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testResourcePathJavaScript() {
        //given
        WebResponse response = WebRequest.get("/assets/javascript/jquery.min.js").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo("application/javascript"));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testResourcePathStylesheet() {
        //given
        WebResponse response = WebRequest.get("/assets/stylesheet/css.css").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo("text/css"));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
}