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
        String requestUri = "/foo";
        String queryString = "?foo=bar";
        String header = "123456789:48B2790431BC556C2EFD69A06A30763D23B47322FF52E1197FA176B56AB925451B016F6C79E001BC9AFAC1F48D2DF8E2D6009B94D3B68E90195A3AEEE5FA0E06";
        String token = "token";
        
        //when
        boolean valid = RequestUtils.hasValidAuthentication(requestUri, queryString, header, token);
        
        //then
        assertThat(valid, equalTo(true));
    }
    
    @Test
    public void testHasInvalidAuthentication() {
        //given
        String requestUri = "/foo";
        String queryString = "?foo=bar";
        String header = "123456789:foobar";
        String token = "token";
        
        //when
        boolean valid = RequestUtils.hasValidAuthentication(requestUri, queryString, header, token);
        
        //then
        assertThat(valid, equalTo(false));
    }
}