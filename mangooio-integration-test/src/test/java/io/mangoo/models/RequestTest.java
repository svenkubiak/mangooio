package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;

import io.mangoo.core.Application;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Session;

/**
 * 
 * @author svenkubiak
 *
 */
public class RequestTest {

    public void testBuild() {
        //given
        Request reqeust = new Request()
                .withAuthentication(Application.getInstance(Authentication.class))
                .withBody("bar")
                .withSession(new Session())
                .withParameter(new HashMap<String, String>());
        
        //then
        assertThat(reqeust.getAuthentication() instanceof Authentication, equalTo(true));
        assertThat(reqeust.getSession() instanceof Session, equalTo(true));
        assertThat(reqeust.getBody(), equalTo("bar"));
        assertThat(reqeust.getParameter().size(), equalTo(0));
    }
}