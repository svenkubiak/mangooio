package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;

import io.mangoo.test.utils.ConcurrentRunner;

/**
 * 
 * @author svenkubiak
 *
 */
public class DateUtilsTest {
    
    @Test
    public void testLocalDateTimeToDate() {
        //given
        LocalDateTime localDateTime = LocalDateTime.now();
        
        //then
        assertThat(DateUtils.localDateTimeToDate(localDateTime), not(equalTo(nullValue())));
    }
    
    @Test
    public void testConcurrentLocalDateTimeToDate() throws InterruptedException {
        Runnable runnable = () -> {
            //given
            LocalDateTime localDateTime = LocalDateTime.now();
            
            //then
            assertThat(DateUtils.localDateTimeToDate(localDateTime), not(equalTo(nullValue())));
        };
        
        ConcurrentRunner.create()
        .withRunnable(runnable)
        .withThreads(50)
        .run();
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
        Runnable runnable = () -> {
            //given
            LocalDate localDate = LocalDate.now();
            
            //then
            assertThat(DateUtils.localDateToDate(localDate), not(equalTo(nullValue())));
        };
        
        ConcurrentRunner.create()
        .withRunnable(runnable)
        .withThreads(50)
        .run();
    }
}