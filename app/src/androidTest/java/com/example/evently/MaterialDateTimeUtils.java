package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

import androidx.annotation.IdRes;

/**
 * Helper for working with material date time pickers because no such helpers exist yet...
 */
public final class MaterialDateTimeUtils {
    private MaterialDateTimeUtils() {}

    /**
     * Given the ID of a resource which, upon clicking on, opens date picker, select a future date.
     * @param res ID of the button that opens date picker.
     * @param dateInMonth A date to select (in some month that results in it being present or future but not past)
     * @return Chosen date.
     */
    public static LocalDate selectFutureDate(@IdRes int res, int dateInMonth)
            throws InterruptedException {
        final var today = LocalDate.now();
        final var dateThisMonth = LocalDate.of(today.getYear(), today.getMonth(), dateInMonth);

        // We are gonna target the next month because it's guaranteed to be in the future.
        final var targetDate = dateThisMonth.plusMonths(1);
        final var targetMonth =
                targetDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        // This should open the selection date picker
        onView(withId(res)).perform(click());
        final var targetDay =
                targetDate.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);

        // Go to the "next month" page on the date picker.
        onView(withContentDescription("Change to next month"))
                .inRoot(isDialog())
                .perform(click());
        // The animation takes a bit of time and while it's happening, the view cannot be interacted
        // with.
        Thread.sleep(500);

        // Fill in the date in the date picker
        onView(withContentDescription(targetDay + ", " + targetMonth + " "
                        + targetDate.getDayOfMonth())) // e.g Saturday, November 29
                .inRoot(isDialog())
                .perform(click());
        onView(withText("OK")).inRoot(isDialog()).perform(click());

        return targetDate;
    }

    /**
     * Given the ID of a resource which, upon clicking on, opens time picker, select a time (in AM).
     * @param res ID of the button that opens time picker.
     * @param hour Must be between 1-12
     * @param minute Must be a multiple of 5 (0 - 55)
     */
    public static LocalTime selectTimeInAM(@IdRes int res, int hour, int minute) {
        // This should open the selection date picker
        onView(withId(res)).perform(click());
        // Fill in the time part
        onView(withContentDescription(hour + " o'clock")) // e.g 12 o'clock
                .inRoot(isDialog())
                .perform(click());
        onView(withContentDescription(minute + " minutes")) // e.g 35 minutes
                .inRoot(isDialog())
                .perform(click());

        onView(withText("OK")).inRoot(isDialog()).perform(click());

        return LocalTime.of(hour, minute);
    }
}
