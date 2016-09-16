package io.mangoo.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Utility class for converting between Date and LocalDateTime, LocalDate and LocalTime
 * 
 * @author svenkubiak
 *
 */
public final class DateUtils {

    /**
     * Converts a Date to LocalDateTime
     * 
     * @param date The Date to convert
     * @return The converted LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        Objects.requireNonNull(date, "date can not be null");
        
        Instant instant = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
    
    /**
     * Converts a Date to LocalDate
     * 
     * @param date The Date to convert
     * @return The converted LocalDate
     */
    public static LocalDate dateToLocalDate(Date date) {
        Objects.requireNonNull(date, "date can not be null");
        
        Instant instant = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
    }
    
    /**
     * Converts a Date to LocalTime
     * 
     * @param date The Date to convert
     * @return The converted LocalTime
     */
    public static LocalTime dateToLocalTime(Date date) {
        Objects.requireNonNull(date, "date can not be null");
        
        Instant instant = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
    }
    
    /**
     * Converts a LocalDateTime to Date
     * 
     * @param localDateTime The LocalDateTime to convert
     * @return The converted Date
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, "localDateTime can not be null");
        
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
        Objects.requireNonNull(localDate, "localDate can not be null");
        
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }
}