package io.mangoo.utils;

import io.mangoo.constants.NotNull;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public final class DateUtils {
    private static final PrettyTime PRETTY_TIME = new PrettyTime(Locale.forLanguageTag("en-EN"));
    private DateUtils() {
    }

    /**
     * Converts a LocalDateTime to Date
     * 
     * @param localDateTime The LocalDateTime to convert
     * @return The converted Date
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, NotNull.LOCAL_DATE_TIME);

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Converts a localDate to Date
     * 
     * @param localDate The LocalDate to convert
     * @return The converted Date
     */
    public static Date localDateToDate(LocalDate localDate) {
        Objects.requireNonNull(localDate, NotNull.LOCAL_DATE);

        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Returns a human-readable, relative timestamps like "moments ago", default to en-EN
     *
     * @param localDateTime The LocalDateTime to base the parsing on
     * @return timestamps like "moments ago"
     */
    public static String getPrettyTime(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, NotNull.LOCAL_DATE_TIME);

        return PRETTY_TIME.format(localDateTime);
    }

    /**
     * Returns a human-readable, relative timestamps like "moments ago" localized based on the given locale
     *
     * @param locale The local to use
     * @param localDateTime like "moments ago"
     * @return timestamps like "moments ago"
     */
    public static String getPrettyTime(Locale locale, LocalDateTime localDateTime) {
        Objects.requireNonNull(locale, NotNull.LOCALE);
        Objects.requireNonNull(localDateTime, NotNull.LOCAL_DATE_TIME);

        return new PrettyTime(locale).format(localDateTime);
    }
}