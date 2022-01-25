package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;

@ExtendWith({TestExtension.class})
public class DateUtilsTest {
    
    @Test
    void testLocalDateTimeToDate() {
        //given
        LocalDateTime localDateTime = LocalDateTime.now();
        
        //then
        assertThat(DateUtils.localDateTimeToDate(localDateTime), not(equalTo(nullValue())));
    }
    
    @Test
    void testConcurrentLocalDateTimeToDate() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            LocalDateTime localDateTime = LocalDateTime.now();
            
            // then
            return DateUtils.localDateTimeToDate(localDateTime) != null;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testLocalDateToDate() {
        //given
        LocalDate localDate = LocalDate.now();
        
        //then
        assertThat(DateUtils.localDateToDate(localDate), not(equalTo(nullValue())));
    }
    
    @Test
    void testConcurrentLocalDateToDate() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            LocalDateTime localDateTime = LocalDateTime.now();
            
            // then
            return DateUtils.localDateTimeToDate(localDateTime) != null;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
}