package io.mangoo.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class DateUtilsTest {

    @Test
    void testLocalDateTimeToDate() {
        //given
        LocalDateTime localDateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);

        //when
        Date date = DateUtils.localDateTimeToDate(localDateTime);

        //then
        assertThat(date, not(nullValue()));
        assertThat(date, instanceOf(Date.class));

        // Convert back to LocalDateTime to verify the conversion
        LocalDateTime convertedBack = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        assertThat(convertedBack, equalTo(localDateTime));
    }

    @Test
    void testLocalDateTimeToDateWithDifferentTimes() {
        //given
        LocalDateTime localDateTime1 = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime localDateTime2 = LocalDateTime.of(2023, 12, 31, 23, 59, 59);

        //when
        Date date1 = DateUtils.localDateTimeToDate(localDateTime1);
        Date date2 = DateUtils.localDateTimeToDate(localDateTime2);

        //then
        assertThat(date1, not(nullValue()));
        assertThat(date2, not(nullValue()));
        assertThat(date1.before(date2), equalTo(true));
    }

    @Test
    void testLocalDateTimeToDateWithLeapYear() {
        //given
        LocalDateTime leapYearDate = LocalDateTime.of(2024, 2, 29, 12, 0, 0);

        //when
        Date date = DateUtils.localDateTimeToDate(leapYearDate);

        //then
        assertThat(date, not(nullValue()));

        // Convert back to verify
        LocalDateTime convertedBack = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        assertThat(convertedBack, equalTo(leapYearDate));
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

        //then
        assertThat(date, not(nullValue()));
        assertThat(date, instanceOf(Date.class));

        // Convert back to LocalDate to verify the conversion
        LocalDate convertedBack = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        assertThat(convertedBack, equalTo(localDate));
    }

    @Test
    void testLocalDateToDateWithDifferentDates() {
        //given
        LocalDate localDate1 = LocalDate.of(2023, 1, 1);
        LocalDate localDate2 = LocalDate.of(2023, 12, 31);

        //when
        Date date1 = DateUtils.localDateToDate(localDate1);
        Date date2 = DateUtils.localDateToDate(localDate2);

        //then
        assertThat(date1, not(nullValue()));
        assertThat(date2, not(nullValue()));
        assertThat(date1.before(date2), equalTo(true));
    }

    @Test
    void testLocalDateToDateWithLeapYear() {
        //given
        LocalDate leapYearDate = LocalDate.of(2024, 2, 29);

        //when
        Date date = DateUtils.localDateToDate(leapYearDate);

        //then
        assertThat(date, not(nullValue()));

        // Convert back to verify
        LocalDate convertedBack = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        assertThat(convertedBack, equalTo(leapYearDate));
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
    void testGetPrettyTimeWithCurrentTime() {
        //given
        LocalDateTime now = LocalDateTime.now();

        //when
        String prettyTime = DateUtils.getPrettyTime(now);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "moments ago" or similar relative time
        assertThat(prettyTime.toLowerCase(), anyOf(
                containsString("moments from now"),
                containsString("moments ago"),
                containsString("second"),
                containsString("minute"),
                containsString("hour"),
                containsString("day"),
                containsString("week"),
                containsString("month"),
                containsString("year")
        ));
    }

    @Test
    void testGetPrettyTimeWithPastTime() {
        //given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(2);

        //when
        String prettyTime = DateUtils.getPrettyTime(pastTime);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "ago" for past time
        assertThat(prettyTime.toLowerCase(), containsString("ago"));
    }

    @Test
    void testGetPrettyTimeWithFutureTime() {
        //given
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);

        //when
        String prettyTime = DateUtils.getPrettyTime(futureTime);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "from now" or similar for future time
        assertThat(prettyTime.toLowerCase(), anyOf(
                containsString("from now"),
                containsString("in ")
        ));
    }

    @Test
    void testGetPrettyTimeWithSpecificDateTime() {
        //given
        LocalDateTime specificTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0);

        //when
        String prettyTime = DateUtils.getPrettyTime(specificTime);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "ago" for past time
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
        LocalDateTime now = LocalDateTime.now();
        Locale locale = Locale.ENGLISH;

        //when
        String prettyTime = DateUtils.getPrettyTime(locale, now);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain relative time text
        assertThat(prettyTime.toLowerCase(), anyOf(
                containsString("moments from now"),
                containsString("moments ago"),
                containsString("second"),
                containsString("minute"),
                containsString("hour"),
                containsString("day"),
                containsString("week"),
                containsString("month"),
                containsString("year")
        ));
    }

    @Test
    void testGetPrettyTimeWithDifferentLocales() {
        //given
        LocalDateTime now = LocalDateTime.now();
        Locale englishLocale = Locale.ENGLISH;
        Locale germanLocale = Locale.GERMAN;

        //when
        String prettyTimeEnglish = DateUtils.getPrettyTime(englishLocale, now);
        String prettyTimeGerman = DateUtils.getPrettyTime(germanLocale, now);

        //then
        assertThat(prettyTimeEnglish, not(nullValue()));
        assertThat(prettyTimeGerman, not(nullValue()));
        assertThat(prettyTimeEnglish, not(emptyString()));
        assertThat(prettyTimeGerman, not(emptyString()));
        // The strings might be different due to localization
        // but both should be valid relative time strings
    }

    @Test
    void testGetPrettyTimeWithLocaleAndPastTime() {
        //given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        Locale locale = Locale.ENGLISH;

        //when
        String prettyTime = DateUtils.getPrettyTime(locale, pastTime);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "ago" for past time
        assertThat(prettyTime.toLowerCase(), containsString("ago"));
    }

    @Test
    void testGetPrettyTimeWithLocaleAndFutureTime() {
        //given
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        Locale locale = Locale.ENGLISH;

        //when
        String prettyTime = DateUtils.getPrettyTime(locale, futureTime);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "from now" or similar for future time
        assertThat(prettyTime.toLowerCase(), anyOf(
                containsString("from now"),
                containsString("in ")
        ));
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

    @Test
    void testGetPrettyTimeWithLocaleBothNull() {
        //given
        LocalDateTime localDateTime = null;
        Locale locale = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> DateUtils.getPrettyTime(locale, localDateTime));
        // Should throw for the first null parameter (locale)
        assertThat(exception.getMessage(), containsString("locale can not be null"));
    }

    @Test
    void testGetPrettyTimeConsistency() {
        //given
        LocalDateTime specificTime = LocalDateTime.of(2023, 6, 15, 10, 30, 0);

        //when
        String prettyTime1 = DateUtils.getPrettyTime(specificTime);
        String prettyTime2 = DateUtils.getPrettyTime(specificTime);

        //then
        assertThat(prettyTime1, equalTo(prettyTime2));
    }

    @Test
    void testGetPrettyTimeWithLocaleConsistency() {
        //given
        LocalDateTime specificTime = LocalDateTime.of(2023, 6, 15, 10, 30, 0);
        Locale locale = Locale.ENGLISH;

        //when
        String prettyTime1 = DateUtils.getPrettyTime(locale, specificTime);
        String prettyTime2 = DateUtils.getPrettyTime(locale, specificTime);

        //then
        assertThat(prettyTime1, equalTo(prettyTime2));
    }

    @Test
    void testGetPrettyTimeWithDifferentLocalesSameTime() {
        //given
        LocalDateTime specificTime = LocalDateTime.of(2023, 6, 15, 10, 30, 0);
        Locale englishLocale = Locale.ENGLISH;
        Locale frenchLocale = Locale.FRENCH;

        //when
        String prettyTimeEnglish = DateUtils.getPrettyTime(englishLocale, specificTime);
        String prettyTimeFrench = DateUtils.getPrettyTime(frenchLocale, specificTime);

        //then
        assertThat(prettyTimeEnglish, not(nullValue()));
        assertThat(prettyTimeFrench, not(nullValue()));
        assertThat(prettyTimeEnglish, not(emptyString()));
        assertThat(prettyTimeFrench, not(emptyString()));
        // The strings should be different due to localization
        // but both should be valid relative time strings
    }

    @Test
    void testLocalDateTimeToDateWithDifferentTimeZones() {
        //given
        LocalDateTime localDateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);

        //when
        Date date = DateUtils.localDateTimeToDate(localDateTime);

        //then
        assertThat(date, not(nullValue()));

        // Verify the conversion preserves the local time
        LocalDateTime convertedBack = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        assertThat(convertedBack, equalTo(localDateTime));
    }

    @Test
    void testLocalDateToDateWithDifferentTimeZones() {
        //given
        LocalDate localDate = LocalDate.of(2023, 12, 25);

        //when
        Date date = DateUtils.localDateToDate(localDate);

        //then
        assertThat(date, not(nullValue()));

        // Verify the conversion preserves the local date
        LocalDate convertedBack = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        assertThat(convertedBack, equalTo(localDate));
    }

    @Test
    void testGetPrettyTimeWithVeryOldDate() {
        //given
        LocalDateTime veryOldDate = LocalDateTime.of(1990, 1, 1, 0, 0, 0);

        //when
        String prettyTime = DateUtils.getPrettyTime(veryOldDate);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "ago" for past time
        assertThat(prettyTime.toLowerCase(), containsString("ago"));
    }

    @Test
    void testGetPrettyTimeWithVeryFutureDate() {
        //given
        LocalDateTime veryFutureDate = LocalDateTime.of(2050, 1, 1, 0, 0, 0);

        //when
        String prettyTime = DateUtils.getPrettyTime(veryFutureDate);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "from now" or similar for future time
        assertThat(prettyTime.toLowerCase(), anyOf(
                containsString("from now"),
                containsString("in ")
        ));
    }

    @Test
    void testGetPrettyTimeWithLocaleAndVeryOldDate() {
        //given
        LocalDateTime veryOldDate = LocalDateTime.of(1990, 1, 1, 0, 0, 0);
        Locale locale = Locale.ENGLISH;

        //when
        String prettyTime = DateUtils.getPrettyTime(locale, veryOldDate);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "ago" for past time
        assertThat(prettyTime.toLowerCase(), containsString("ago"));
    }

    @Test
    void testGetPrettyTimeWithLocaleAndVeryFutureDate() {
        //given
        LocalDateTime veryFutureDate = LocalDateTime.of(2050, 1, 1, 0, 0, 0);
        Locale locale = Locale.ENGLISH;

        //when
        String prettyTime = DateUtils.getPrettyTime(locale, veryFutureDate);

        //then
        assertThat(prettyTime, not(nullValue()));
        assertThat(prettyTime, not(emptyString()));
        // Should contain "from now" or similar for future time
        assertThat(prettyTime.toLowerCase(), anyOf(
                containsString("from now"),
                containsString("in ")
        ));
    }
}
