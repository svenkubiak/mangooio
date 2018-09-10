package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;

/**
 * 
 * @author svenkubiak
 *
 */
public class CryptoUtilsTest {

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
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test()
    public void testInvalidMinRandomString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //given
            CryptoUtils.randomString(0);
          });
    }
    
    @Test()
    public void testInvalidMaxRandomString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CryptoUtils.randomString(257);
          });
    }
}