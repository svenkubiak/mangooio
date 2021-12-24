package io.mangoo.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import io.mangoo.enums.Required;

/**
 * Utility class for converting between Date, LocalDateTime and LocalDate
 * 
 * @author svenkubiak
 *
 */
public final class DateUtils {
    private DateUtils() {
    }

    /**
     * Converts a LocalDateTime to Date
     * 
     * @param localDateTime The LocalDateTime to convert
     * @return The converted Date
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, Required.LOCAL_DATE_TIME.toString());
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Converts a localDate to Date
     * 
     * @param localDate The LocalDate to convert
     * @return The converted Date
     */
    public static Date localDateToDate(LocalDate localDate) {
        Objects.requireNonNull(localDate, Required.LOCAL_DATE.toString());
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}