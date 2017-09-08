package io.mangoo.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Optional;

import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.enums.oauth.OAuthProvider;

/**
 * 
 * @author svenkubiak
 *
 */
public class RequestHelperTest {

	@Test
	public void testGetOAuthProvider() {
        //given
    		RequestHelper helper = Application.getInstance(RequestHelper.class);
    		
    		//when
    		Optional<OAuthProvider> twitter = helper.getOAuthProvider("twitter");
    		Optional<OAuthProvider> facebook = helper.getOAuthProvider("facebook");
    		Optional<OAuthProvider> google = helper.getOAuthProvider("google");
    	
        //then
        assertThat(twitter, not(nullValue()));
        assertThat(twitter.isPresent(), equalTo(true));
        assertThat(twitter.get().name(), equalTo("TWITTER"));
        assertThat(facebook, not(nullValue()));
        assertThat(facebook.isPresent(), equalTo(true));
        assertThat(facebook.get().name(), equalTo("FACEBOOK"));
        assertThat(google, not(nullValue()));
        assertThat(google.isPresent(), equalTo(true));
        assertThat(google.get().name(), equalTo("GOOGLE"));
	}
}