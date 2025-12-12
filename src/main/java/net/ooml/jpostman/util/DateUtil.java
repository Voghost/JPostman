package net.ooml.jpostman.util;

import net.ooml.jpostman.config.Constants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Date utility class
 */
public class DateUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern(Constants.TIME_FORMAT);

    /**
     * Format LocalDateTime to string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Format LocalDateTime to date string
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * Format LocalDateTime to time string
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * Parse string to LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (StringUtil.isEmpty(dateTimeStr)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }

    /**
     * Convert Date to LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Convert LocalDateTime to Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Get current timestamp as LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    private DateUtil() {
        // Prevent instantiation
    }
}
