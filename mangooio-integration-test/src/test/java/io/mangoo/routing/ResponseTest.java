package io.mangoo.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class ResponseTest {
    
    @Test
    public void testAndConent() {
        //given
        Response response = new Response();
        
        //when
        response.andContent("foo", null);
        response.andContent("foo2", "bar");
        
        //then
        assertThat(response.getContent(), not(nullValue()));
        assertThat(response.getContent().get("foo"), equalTo(null));
        assertThat(response.getContent().get("foo2"), equalTo("bar"));
    }
}