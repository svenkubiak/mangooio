package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class DateUtilsTest {

    @Test
    public void testDateToLocalDateTime() {
        //given
        Date date = new Date();
        
        //then
        assertThat(DateUtils.dateToLocalDateTime(date), not(equalTo(nullValue())));
    }
    
    @Test
    public void testDateToLocalDate() {
        //given
        Date date = new Date();
        
        //then
        assertThat(DateUtils.dateToLocalDate(date), not(equalTo(nullValue())));
    }
    
    @Test
    public void testDateToLocalTime() {
        //given
        Date date = new Date();
        
        //then
        assertThat(DateUtils.dateToLocalTime(date), not(equalTo(nullValue())));
    }
    
    @Test
    public void testLocalDateTimeToDate() {
        //given
        LocalDateTime localDateTime = LocalDateTime.now();
        
        //then
        assertThat(DateUtils.localDateTimeToDate(localDateTime), not(equalTo(nullValue())));
    }
    
    @Test
    public void testLocalDateToDate() {
        //given
        LocalDate localDate = LocalDate.now();
        
        //then
        assertThat(DateUtils.localDateToDate(localDate), not(equalTo(nullValue())));
    }
}
