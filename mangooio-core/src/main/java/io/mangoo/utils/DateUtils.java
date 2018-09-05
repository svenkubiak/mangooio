package io.mangoo.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import io.mangoo.enums.Required;

/**
 * Utility class for converting between Date and LocalDateTime, LocalDate and LocalTime
 * 
 * @author svenkubiak
 *
 */
public final class DateUtils {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
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
        
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }
    
    /**
     * Converts a localDate to Date
     * 
     * @param localDate The LocalDate to convert
     * @return The converted Date
     */
    public static Date localDateToDate(LocalDate localDate) {
        Objects.requireNonNull(localDate, Required.LOCAL_DATE.toString());
        
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }
}