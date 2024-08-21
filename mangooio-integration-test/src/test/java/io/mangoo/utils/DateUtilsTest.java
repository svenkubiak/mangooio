package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.test.concurrent.ConcurrentRunner;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Execution(ExecutionMode.CONCURRENT)
class DateUtilsTest {
    
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
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            
            // then
            return DateUtils.localDateTimeToDate(localDateTime).equals(date);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
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
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            
            // then
            return DateUtils.localDateTimeToDate(localDateTime).equals(date);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }

    @Test
    void testPrettyTime() {
        //given
        LocalDateTime localDateTime = LocalDateTime.now();

        //then
        assertThat(DateUtils.getPrettyTime(localDateTime), not(equalTo(nullValue())));
    }

    @Test
    void testConcurrentPrettyTime() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            LocalDateTime localDateTime = LocalDateTime.now();

            // then
            return DateUtils.getPrettyTime(localDateTime) != null;
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }

    @Test
    void testLocalizedPrettyTime() {
        //given
        LocalDateTime localDateTime = LocalDateTime.now();

        //then
        assertThat(DateUtils.getPrettyTime(Locale.forLanguageTag("de-DE"), localDateTime), not(equalTo(nullValue())));
    }

    @Test
    void testConcurrentLocalizedPrettyTime() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            LocalDateTime localDateTime = LocalDateTime.now();

            // then
            return DateUtils.getPrettyTime(Locale.forLanguageTag("de-DE"), localDateTime) != null;
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
}