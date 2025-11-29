package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.annotation.IdRes;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Helper for working with material date time pickers because no such helpers exist yet...
 */
public final class MaterialDateTimeUtils {
    private MaterialDateTimeUtils() {}

    /**
     * Given the ID of a resource which, upon clicking on, opens date picker, select a date in current month.
     * @param res ID of the button that opens date picker.
     * @param dateInMonth A date to select (in current month)
     */
    public static void selectDateInMonth(@IdRes int res, int dateInMonth) {
        final var today = LocalDate.now();
        final var targetDate = LocalDate.of(today.getYear(), today.getMonth(), dateInMonth);
        final var targetMonth = today.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        // This should open the selection date picker
        onView(withId(res)).perform(click());
        final var selectionDay = targetDate.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        // Fill in the date in the date picker
        onView(withContentDescription(selectionDay + ", " + targetMonth + " " + dateInMonth)) // e.g Saturday, November 29
                .inRoot(isDialog())
                .perform(click());
        onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click());
    }

    /**
     * Given the ID of a resource which, upon clicking on, opens time picker, select a time (in AM).
     * @param res ID of the button that opens time picker.
     * @param hour Must be between 1-12
     * @param minute Must be a multiple of 5 (0 - 55)
     */
    public static void selectTimeInAM(@IdRes int res, int hour, int minute) {
        // This should open the selection date picker
        onView(withId(res)).perform(click());
        // Fill in the time part
        onView(withContentDescription(hour + " o'clock")) // e.g 12 o'clock
                .inRoot(isDialog())
                .perform(click());
        onView(withContentDescription(minute + " minutes")) // e.g 35 minutes
                .inRoot(isDialog())
                .perform(click());

        onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click());
    }
}
