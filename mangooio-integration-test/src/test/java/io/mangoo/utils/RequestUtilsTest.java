package io.mangoo.utils;

import static org.junit.Assert.assertEquals;

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
        assertEquals("template.ftl", RequestUtils.getTemplateName("template.ftl"));
        assertEquals("template.ftl", RequestUtils.getTemplateName("template"));
    }
    
    @Test
    public void testGetOAuthProvider() {
        assertEquals(OAuthProvider.TWITTER, RequestUtils.getOAuthProvider("twitter"));
        assertEquals(OAuthProvider.GOOGLE, RequestUtils.getOAuthProvider("google"));
        assertEquals(OAuthProvider.FACEBOOK, RequestUtils.getOAuthProvider("facebook"));
    }
}