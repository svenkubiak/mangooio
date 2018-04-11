package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.cactoos.matchers.RunsInThreads;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class DateUtilsTest {
    private static final int THREADS = 100;
    
    @Test
    public void testLocalDateTimeToDate() {
        //given
        LocalDateTime localDateTime = LocalDateTime.now();
        
        //then
        assertThat(DateUtils.localDateTimeToDate(localDateTime), not(equalTo(nullValue())));
    }
    
    @Test
    public void testConcurrentLocalDateTimeToDate() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            LocalDateTime localDateTime = LocalDateTime.now();
            
            // then
            return DateUtils.localDateTimeToDate(localDateTime) != null;
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
    }
    
    @Test
    public void testLocalDateToDate() {
        //given
        LocalDate localDate = LocalDate.now();
        
        //then
        assertThat(DateUtils.localDateToDate(localDate), not(equalTo(nullValue())));
    }
    
    @Test
    public void testConcurrentLocalDateToDate() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            LocalDateTime localDateTime = LocalDateTime.now();
            
            // then
            return DateUtils.localDateTimeToDate(localDateTime) != null;
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
    }
}