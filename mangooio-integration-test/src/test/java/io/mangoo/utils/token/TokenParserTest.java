package io.mangoo.utils.token;

import io.mangoo.TestExtension;
import io.mangoo.constants.ClaimKey;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.mangoo.utils.MangooUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@ExtendWith({TestExtension.class})
class TokenParserTest {
    
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
        String claimKey = ClaimKey.DATA;
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
    
    @Test
    void testConcurrentParse() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            LocalDateTime expires = LocalDateTime.now().plusDays(1);
            String sharedSecret = MangooUtils.randomString(32);
            String subject = MangooUtils.randomString(32);
            String claimKey = ClaimKey.DATA;
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
            return token != null &&
                   token.getExpiration().equals(expires) &&
                   token.getSubject().equals(subject) &&
                   token.getClaim(claimKey, String.class).equals(claimValue);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
}
