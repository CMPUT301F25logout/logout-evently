package com.example.evently.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class DateTimeUtils {

    /**
     * Converts an epoch millis to a local datetime.
     * @param instant The instant to convert.
     * @return The converted local datetime.
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Treat the given epoch millis as local date.
     * @param epochMillis The epoch milliseconds to convert.
     * @return The converted local date.
     */
    public static LocalDate treatAsLocalDate(long epochMillis) {
        // The "atZone(UTC) basically tells Instant: Don't do any conversion, it's already in system
        // default time zone.
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDate();
    }

    /**
     * Converts a local date to an epoch millis.
     * @param localDate The local date to convert.
     * @return The converted epoch millis.
     */
    public static long toEpochMillis(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private DateTimeUtils() {}
}
