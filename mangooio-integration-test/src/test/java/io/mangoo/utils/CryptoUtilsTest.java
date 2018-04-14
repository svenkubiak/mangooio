package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.atomic.AtomicInteger;

import org.cactoos.matchers.RunsInThreads;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class CryptoUtilsTest {
    private static final String VALID_SECRET = "jklfdjskjfkldsnjkvbnxjk<ffdsjklfdjhkfhyjkv<hjfkdbshjfkbshfbdvhjlbufsjkfdsjkfdhsjkfdvcxvcx";
    private static final String INVALID_SECRET = "fdsfdsf";
    private static final int THREADS = 100;

    @Test
    public void testGetSizedKey() {
        //given
        String secret = CryptoUtils.getSizedSecret(VALID_SECRET);
        
        //then
        assertThat(secret, not(nullValue()));
        assertThat(secret.length(), equalTo(32));
    }
    
    @Test
    public void testConcurrentGetSizedKey() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            // given
            String secret = CryptoUtils.getSizedSecret(VALID_SECRET);
            
            // then
            return secret.length() == 32;
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
    }
    
    @Test
    public void testIsValidSecret() {
        //given
        boolean valid = CryptoUtils.isValidSecret(VALID_SECRET);
        
        //then
        assertThat(valid, equalTo(true));
    }
    
    @Test
    public void testConcurrentIsValidSecret() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            boolean valid = CryptoUtils.isValidSecret(VALID_SECRET);
            
            // then
            return valid;
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
    }
    
    @Test
    public void testIsInvalidSecret() {
        //given
        boolean valid = CryptoUtils.isValidSecret(INVALID_SECRET);
        
        //then
        assertThat(valid, equalTo(false));
    }
    
    @Test
    public void testConcurrentIsInvalidSecret() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            boolean valid = CryptoUtils.isValidSecret(INVALID_SECRET);
            
            // then
            return !valid;
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
    }
    
    @Test
    public void testRandomString() {
        //given
        String string = CryptoUtils.randomString(32);
        
        //then
        assertThat(string, not(nullValue()));
        assertThat(string.length(), equalTo(32));
    }
    
    @Test
    public void testConcurrentRandomString() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            // given
            int size = (int) (Math.random() * (64 - 16)) + 16;
            String secret = CryptoUtils.randomString(size);
            
            // then
            return secret.length() == size;
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMinRandomString() {
        //given
        CryptoUtils.randomString(0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMaxRandomString() {
        //given
        CryptoUtils.randomString(257);
    }
}