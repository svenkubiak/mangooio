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
class TokenBuilderTest {
    
    @Test
    void testWithExpipres() {
        //given
        LocalDateTime now = LocalDateTime.now();
        
        //when
        TokenBuilder tokenBuilder = TokenBuilder.create().withExpires(now);
        
        //then
        assertThat(tokenBuilder.getExpires(), equalTo(now));
    }
    
    @Test
    void testWithSharedSecret() {
        //given
        String sharedSecret = MangooUtils.randomString(32);
        
        //when
        TokenBuilder tokenBuilder = TokenBuilder.create().withSharedSecret(sharedSecret);
        
        //then
        assertThat(tokenBuilder.getSharedSecret(), equalTo(sharedSecret));
    }
    
    @Test
    void testWithClaim() {
        //given
        String claimKey = ClaimKey.DATA;
        String value = MangooUtils.randomString(32);
        
        //when
        TokenBuilder tokenBuilder = TokenBuilder.create().withClaim(claimKey, value);
        
        //then
        assertThat(tokenBuilder.getClaims().get(claimKey), equalTo(value));
    }
    
    @Test
    void testWithSubject() {
        //given
        String subject = MangooUtils.randomString(32);
        
        //when
        TokenBuilder tokenBuilder = TokenBuilder.create().withSubject(subject);
        
        //then
        assertThat(tokenBuilder.getSubject(), equalTo(subject));
    }
    
    @Test
    void testBuild() throws MangooTokenException {
        //given
        LocalDateTime now = LocalDateTime.now();
        String sharedSecret = MangooUtils.randomString(32);
        String subject = MangooUtils.randomString(32);
        String claimKey = ClaimKey.DATA;
        String value = MangooUtils.randomString(32);
        
        //when
        String token = TokenBuilder.create()
                .withExpires(now)
                .withSharedSecret(sharedSecret)
                .withClaim(claimKey, value)
                .withSubject(subject)
                .build();
        
        //then
        assertThat(token, not(equalTo(null)));
    }
    
    @Test
    void testConcurrentBuild() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            LocalDateTime now = LocalDateTime.now();
            String sharedSecret = MangooUtils.randomString(32);
            String subject = MangooUtils.randomString(32);
            String claimKey = ClaimKey.DATA;
            String value = MangooUtils.randomString(32);
            
            //when
            String token = TokenBuilder.create()
                    .withExpires(now)
                    .withSharedSecret(sharedSecret)
                    .withClaim(claimKey, value)
                    .withSubject(subject)
                    .build();
            
            //then
            return token != null;
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
}