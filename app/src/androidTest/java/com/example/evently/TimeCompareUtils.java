package com.example.evently;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

// Helper for comparing times.
public final class TimeCompareUtils {

    /**
     * Compare a UTC datetime with a local (system default) datetime.
     * @param utcTime A datetime represented in UTC.
     * @param localDateTime A raw datetime that is meant to be contextual under the system default timezone.
     * @return true if the two times are equivalent.
     */
    public static boolean compareUTC(Instant utcTime, LocalDateTime localDateTime) {
        return utcTime.equals(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private TimeCompareUtils() {}
}
