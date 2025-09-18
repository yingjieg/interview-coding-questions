package com.example.demo.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utility class for common date operations to reduce code duplication.
 */
public final class DateUtils {

    private DateUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns tomorrow's date (current date + 1 day).
     * Commonly used for visit date validations.
     *
     * @return tomorrow's date
     */
    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    /**
     * Returns current date.
     * Provided for consistency with other date utility methods.
     *
     * @return current date
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Returns current date and time.
     * Provided for consistency with other date utility methods.
     *
     * @return current date and time
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Checks if a given date is in the past (before today).
     *
     * @param date the date to check
     * @return true if the date is before today, false otherwise
     */
    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(today());
    }

    /**
     * Checks if a given date/time has expired (is before current time).
     *
     * @param expiryTime the date/time to check
     * @return true if expired, false otherwise
     */
    public static boolean isExpired(LocalDateTime expiryTime) {
        return expiryTime != null && expiryTime.isBefore(now());
    }
}