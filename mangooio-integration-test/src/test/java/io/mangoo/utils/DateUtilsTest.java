package io.mangoo.utils;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
class DateUtilsTest {

    @Test
    void testLocalDateTimeToDate() {
        //given
        LocalDateTime localDateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);

        //when
        Date date = DateUtils.localDateTimeToDate(localDateTime);

        //then the configured application time zone (Europe/Berlin, UTC+1 in winter) is used
        assertThat(date.toInstant(), equalTo(Instant.parse("2023-12-25T13:30:45Z")));
    }

    @Test
    void testLocalDateTimeToDateNullInput() {
        //given
        LocalDateTime localDateTime = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> DateUtils.localDateTimeToDate(localDateTime));
        assertThat(exception.getMessage(), containsString("localDateTime can not be null"));
    }

    @Test
    void testLocalDateToDate() {
        //given
        LocalDate localDate = LocalDate.of(2023, 12, 25);

        //when
        Date date = DateUtils.localDateToDate(localDate);

        //then start of day in the configured application time zone (Europe/Berlin, UTC+1 in winter)
        assertThat(date.toInstant(), equalTo(Instant.parse("2023-12-24T23:00:00Z")));
    }

    @Test
    void testLocalDateToDateNullInput() {
        //given
        LocalDate localDate = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> DateUtils.localDateToDate(localDate));
        assertThat(exception.getMessage(), containsString("localDate can not be null"));
    }

    @Test
    void testGetPrettyTimeWithPastTime() {
        //given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(2);

        //when
        String prettyTime = DateUtils.getPrettyTime(pastTime);

        //then
        assertThat(prettyTime.toLowerCase(), containsString("ago"));
    }

    @Test
    void testGetPrettyTimeNullInput() {
        //given
        LocalDateTime localDateTime = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> DateUtils.getPrettyTime(localDateTime));
        assertThat(exception.getMessage(), containsString("localDateTime can not be null"));
    }

    @Test
    void testGetPrettyTimeWithLocale() {
        //given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(2);

        //when
        String prettyTime = DateUtils.getPrettyTime(Locale.GERMAN, pastTime);

        //then
        assertThat(prettyTime, startsWith("vor "));
    }

    @Test
    void testGetPrettyTimeWithLocaleNullLocale() {
        //given
        LocalDateTime localDateTime = LocalDateTime.now();
        Locale locale = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> DateUtils.getPrettyTime(locale, localDateTime));
        assertThat(exception.getMessage(), containsString("locale can not be null"));
    }

    @Test
    void testGetPrettyTimeWithLocaleNullLocalDateTime() {
        //given
        LocalDateTime localDateTime = null;
        Locale locale = Locale.ENGLISH;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> DateUtils.getPrettyTime(locale, localDateTime));
        assertThat(exception.getMessage(), containsString("localDateTime can not be null"));
    }
}
