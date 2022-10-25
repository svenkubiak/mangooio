package io.mangoo.utils.token;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.enums.ClaimKey;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.utils.MangooUtils;

@ExtendWith({TestExtension.class})
public class TokenParserTest {
    
    @Test
    void testWithSharedSecret() {
        //given
        String sharedSecret = MangooUtils.randomString(32);
        
        //when
        TokenParser parser = TokenParser.create().withSharedSecret(sharedSecret);
        
        //then
        assertThat(parser.getSharedSecret(), equalTo(sharedSecret));
    }
    
    @Test
    void testWithCookieValue() {
        //given
        String value = MangooUtils.randomString(32);
        
        //when
        TokenParser parser = TokenParser.create().withCookieValue(value);
        
        //then
        assertThat(parser.getCookieValue(), equalTo(value));
    }
    
    @Test
    void testParse() throws MangooTokenException {
        //given
        LocalDateTime expires = LocalDateTime.now().plusDays(1);
        String sharedSecret = MangooUtils.randomString(32);
        String subject = MangooUtils.randomString(32);
        ClaimKey claimKey = ClaimKey.DATA;
        String claimValue = MangooUtils.randomString(32);
        String buildToken = TokenBuilder.create()
                .withExpires(expires)
                .withSharedSecret(sharedSecret)
                .withClaim(claimKey, claimValue)
                .withSubject(subject)
                .build();
        
        //when
        Token token = TokenParser.create().withCookieValue(buildToken).withSharedSecret(sharedSecret).parse();
        
        //then
        assertThat(token, not(equalTo(null)));
        assertThat(token.getExpiration(), equalTo(expires));
        assertThat(token.getSubject(), equalTo(subject));
        assertThat(token.getClaim(claimKey, String.class), equalTo(claimValue));
    }
}
