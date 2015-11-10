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
    
    @Test
    public void testHasValidAuthentication() {
        //given
        String header = "set-cookie:TEST-AUTH=359770bc1a7b38a6dee6ea0ce9875a3d71313f78470174fd460258e4010a51cb2db9c728c5d588958c52d2ef9fe9f6f63ed3aeb4f1ab828e29ce963703eb9237|2999-11-11T11:11:11.111|0#mangooio; path=/; secure; HttpOnly; Expires=Tue, 11-Nov-2999 11:11:11 GMT";
        
        //when
        boolean valid = RequestUtils.hasValidAuthentication(header);
        
        //then
        assertThat(valid, equalTo(true));
    }
    
    @Test
    public void testHasInvalidAuthentication() {
        //given
        String header = null;
        
        //when
        boolean valid = RequestUtils.hasValidAuthentication(header);
        
        //then
        assertThat(valid, equalTo(false));
    }
}