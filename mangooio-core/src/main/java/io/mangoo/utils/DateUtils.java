package io.mangoo.utils;

import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public final class DateUtils {
    private static final PrettyTime PRETTY_TIME = new PrettyTime(Locale.forLanguageTag("en-EN"));
    private DateUtils() {
    }

    /**
     * Uses the configured application time zone.
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, Required.LOCAL_DATE_TIME);
        var config = Application.getInstance(Config.class);

        return Date.from(localDateTime.atZone(config.getApplicationTimeZone()).toInstant());
    }
    
    /**
     * Uses the configured application time zone.
     */
    public static Date localDateToDate(LocalDate localDate) {
        Objects.requireNonNull(localDate, Required.LOCAL_DATE);
        var config = Application.getInstance(Config.class);

        return Date.from(localDate.atStartOfDay()
                .atZone(config.getApplicationTimeZone())
                .toInstant());
    }

    public static String getPrettyTime(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, Required.LOCAL_DATE_TIME);

        return PRETTY_TIME.format(localDateTime);
    }

    public static String getPrettyTime(Locale locale, LocalDateTime localDateTime) {
        Objects.requireNonNull(locale, Required.LOCALE);
        Objects.requireNonNull(localDateTime, Required.LOCAL_DATE_TIME);

        return new PrettyTime(locale).format(localDateTime);
    }
}