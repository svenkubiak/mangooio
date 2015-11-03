package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.mangoo.enums.oauth.OAuthProvider;

/**
 * 
 * @author svenkubiak
 *
 */
public class RequestUtilsTest {

    @Test
    public void testGetTemplateName() {
        //given
        String name1 = RequestUtils.getTemplateName("template.ftl");
        String name2 = RequestUtils.getTemplateName("template");
        
        //then
        assertThat(name1, equalTo("template.ftl"));
        assertThat(name2, equalTo("template.ftl"));
    }
    
    @Test
    public void testGetOAuthProvider() {
        //given
        OAuthProvider twitter = RequestUtils.getOAuthProvider("twitter");
        OAuthProvider google = RequestUtils.getOAuthProvider("google");
        OAuthProvider facebook = RequestUtils.getOAuthProvider("facebook");
        
        //then
        assertThat(twitter, equalTo(OAuthProvider.TWITTER));
        assertThat(google, equalTo(OAuthProvider.GOOGLE));
        assertThat(facebook, equalTo(OAuthProvider.FACEBOOK));
    }
}