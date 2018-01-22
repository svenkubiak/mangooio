package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.test.utils.ConcurrentTester;

/**
 * 
 * @author svenkubiak
 *
 */
public class CryptoUtilsTest {
    private static final String VALID_SECRET = "jklfdjskjfkldsnjkvbnxjk<fbufsjkfdsjkfdhsjkfdvcxvcx";
    private static final String INVALID_SECRET = "fdsfdsf";
    private static final int THREADS = 50;

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
        Runnable runnable = () -> {
            //given
            String secret = CryptoUtils.getSizedSecret(VALID_SECRET);
            
            //then
            assertThat(secret, not(nullValue()));
            assertThat(secret.length(), equalTo(32));
        };
        
        ConcurrentTester.create()
            .withRunnable(runnable)
            .withThreads(THREADS)
            .run();
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
        Runnable runnable = () -> {
            //given
            boolean valid = CryptoUtils.isValidSecret(VALID_SECRET);
            
            //then
            assertThat(valid, equalTo(true));
        };
        
        ConcurrentTester.create()
            .withRunnable(runnable)
            .withThreads(THREADS)
            .run();
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
        Runnable runnable = () -> {
            //given
            boolean valid = CryptoUtils.isValidSecret(INVALID_SECRET);
            
            //then
            assertThat(valid, equalTo(false));
        };
        
        ConcurrentTester.create()
            .withRunnable(runnable)
            .withThreads(THREADS)
            .run();
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
        Runnable runnable = () -> {
            //given
            String string = CryptoUtils.randomString(32);
            
            //then
            assertThat(string, not(nullValue()));
            assertThat(string.length(), equalTo(32));
        };
        
        ConcurrentTester.create()
            .withRunnable(runnable)
            .withThreads(THREADS)
            .run();
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