package com.example.evently.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class DateTimeUtils {

    /**
     * Converts an epoch millis to a local datetime.
     * @param instant The instant to convert.
     * @return The converted local datetime.
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * Converts an epoch millis to a local date.
     * @param epochMillis The epoch milliseconds to convert.
     * @return The converted local date.
     */
    public static LocalDate toLocalDate(long epochMillis) {
        return toLocalDateTime(Instant.ofEpochMilli(epochMillis)).toLocalDate();
    }

    /**
     * Converts a local date to an epoch millis.
     * @param localDate The local date to convert.
     * @return The converted epoch millis.
     */
    public static long toEpochMillis(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    private DateTimeUtils() {}
}
