package com.example.evently.ui.entrant;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.EmulatedFragmentTest;
import com.example.evently.R;
import com.example.evently.ui.model.BrowseEventsViewModel;

/**
 * UI tests for {@link DateFiltersFragment}.
 */
@RunWith(AndroidJUnit4.class)
public class DateFiltersTest extends EmulatedFragmentTest<DateFiltersFragment> {

    @Override
    protected int getGraph() {
        return R.navigation.entrant_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_date_filters;
    }

    @Override
    protected Class<DateFiltersFragment> getFragmentClass() {
        return DateFiltersFragment.class;
    }

    @Test
    public void confirmAppliesValidDateRange() {
        final LocalDate startDate = LocalDate.of(2030, 1, 1);
        final LocalTime startTime = LocalTime.of(9, 30);
        final LocalDate endDate = LocalDate.of(2030, 1, 10);
        final LocalTime endTime = LocalTime.of(18, 45);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", startDate);
            setPrivateField(fragment, "selectedStartTime", startTime);
            setPrivateField(fragment, "selectedEndDate", endDate);
            setPrivateField(fragment, "selectedEndTime", endTime);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertEquals(
                    LocalDateTime.of(startDate, startTime),
                    viewModel.getAfterDateFilter().getValue());
            assertEquals(
                    LocalDateTime.of(endDate, endTime),
                    viewModel.getBeforeDateFilter().getValue());
            assertEquals(startTime, viewModel.getAfterTimeFilter().getValue());
            assertEquals(endTime, viewModel.getBeforeTimeFilter().getValue());

            TextView errorView = fragment.requireView().findViewById(R.id.tvDateValidationError);
            assertEquals(View.GONE, errorView.getVisibility());
        });
    }

    @Test
    public void invalidDateOrderShowsErrorAndDoesNotUpdateViewModel() {
        final LocalDate startDate = LocalDate.of(2030, 2, 1);
        final LocalTime startTime = LocalTime.of(10, 0);
        final LocalDate endDate = LocalDate.of(2030, 1, 1);
        final LocalTime endTime = LocalTime.of(12, 0);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", startDate);
            setPrivateField(fragment, "selectedStartTime", startTime);
            setPrivateField(fragment, "selectedEndDate", endDate);
            setPrivateField(fragment, "selectedEndTime", endTime);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        onView(withId(R.id.tvDateValidationError))
                .check(matches(Matchers.allOf(
                        isDisplayed(), withText(R.string.date_filters_invalid_order))));

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
            assertNull(viewModel.getAfterTimeFilter().getValue());
            assertNull(viewModel.getBeforeTimeFilter().getValue());
        });
    }

    @Test
    public void confirmClearsFiltersWhenNoDatesSelected() {
        final LocalDate previousStart = LocalDate.of(2029, 5, 1);
        final LocalDate previousEnd = LocalDate.of(2029, 6, 1);

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            viewModel.setDateFilters(
                    LocalDateTime.of(previousStart, LocalTime.NOON),
                    LocalDateTime.of(previousEnd, LocalTime.NOON),
                    LocalTime.NOON,
                    LocalTime.NOON);

            setPrivateField(fragment, "selectedStartDate", null);
            setPrivateField(fragment, "selectedEndDate", null);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
            assertNull(viewModel.getAfterTimeFilter().getValue());
            assertNull(viewModel.getBeforeTimeFilter().getValue());
        });
    }

    @Test
    public void blankTimesDefaultToMidnightWhenDatesSelected() {
        final LocalDate startDate = LocalDate.of(2031, 3, 15);
        final LocalDate endDate = LocalDate.of(2031, 3, 20);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", startDate);
            setPrivateField(fragment, "selectedEndDate", endDate);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertEquals(
                    LocalDateTime.of(startDate, LocalTime.MIDNIGHT),
                    viewModel.getAfterDateFilter().getValue());
            assertEquals(
                    LocalDateTime.of(endDate, LocalTime.MIDNIGHT),
                    viewModel.getBeforeDateFilter().getValue());
            assertNull(viewModel.getAfterTimeFilter().getValue());
            assertNull(viewModel.getBeforeTimeFilter().getValue());
        });
    }

    @Test
    public void timeOrderValidatesWhenDatesEqual() {
        final LocalDate date = LocalDate.of(2032, 5, 1);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", date);
            setPrivateField(fragment, "selectedEndDate", date);
            setPrivateField(fragment, "selectedStartTime", LocalTime.of(15, 0));
            setPrivateField(fragment, "selectedEndTime", LocalTime.of(14, 0));
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        onView(withId(R.id.tvDateValidationError))
                .check(matches(Matchers.allOf(
                        isDisplayed(), withText(R.string.date_filters_invalid_order))));

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
            assertNull(viewModel.getAfterTimeFilter().getValue());
            assertNull(viewModel.getBeforeTimeFilter().getValue());
        });
    }

    @Test
    public void clearFilterButtonResetsSelectionsAndViewModel() {
        final LocalDate startDate = LocalDate.of(2035, 7, 1);
        final LocalTime startTime = LocalTime.of(8, 0);
        final LocalDate endDate = LocalDate.of(2035, 7, 2);
        final LocalTime endTime = LocalTime.of(10, 30);

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            viewModel.setDateFilters(
                    LocalDateTime.of(startDate, LocalTime.MIDNIGHT),
                    LocalDateTime.of(endDate, LocalTime.MIDNIGHT),
                    startTime,
                    endTime);

            setPrivateField(fragment, "selectedStartDate", startDate);
            setPrivateField(fragment, "selectedStartTime", startTime);
            setPrivateField(fragment, "selectedEndDate", endDate);
            setPrivateField(fragment, "selectedEndTime", endTime);

            TextView errorView = fragment.requireView().findViewById(R.id.tvDateValidationError);
            errorView.setText("error");
            errorView.setVisibility(View.VISIBLE);
        });

        onView(withText("Clear Filter")).perform(scrollTo(), click());

        onView(withId(R.id.etStartDate)).check(matches(withText("")));
        onView(withId(R.id.etEndDate)).check(matches(withText("")));
        onView(withId(R.id.etStartTime)).check(matches(withText("")));
        onView(withId(R.id.etEndTime)).check(matches(withText("")));
        onView(withId(R.id.tvDateValidationError)).check(matches(Matchers.not(isDisplayed())));

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
            assertNull(viewModel.getAfterTimeFilter().getValue());
            assertNull(viewModel.getBeforeTimeFilter().getValue());
            assertNull(getPrivateField(fragment, "selectedStartDate"));
            assertNull(getPrivateField(fragment, "selectedStartTime"));
            assertNull(getPrivateField(fragment, "selectedEndDate"));
            assertNull(getPrivateField(fragment, "selectedEndTime"));
        });
    }

    @Test
    public void timeOnlyFiltersApplyWithoutDates() {
        final LocalTime startTime = LocalTime.of(11, 15);
        final LocalTime endTime = LocalTime.of(19, 45);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", null);
            setPrivateField(fragment, "selectedEndDate", null);
            setPrivateField(fragment, "selectedStartTime", startTime);
            setPrivateField(fragment, "selectedEndTime", endTime);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
            assertEquals(startTime, viewModel.getAfterTimeFilter().getValue());
            assertEquals(endTime, viewModel.getBeforeTimeFilter().getValue());
        });
    }

    @Test
    public void startTimeOnlyAppliesLowerBound() {
        final LocalTime startTime = LocalTime.of(7, 45);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", null);
            setPrivateField(fragment, "selectedEndDate", null);
            setPrivateField(fragment, "selectedStartTime", startTime);
            setPrivateField(fragment, "selectedEndTime", null);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
            assertEquals(startTime, viewModel.getAfterTimeFilter().getValue());
            assertNull(viewModel.getBeforeTimeFilter().getValue());
        });
    }

    @Test
    public void endTimeOnlyAppliesUpperBound() {
        final LocalTime endTime = LocalTime.of(17, 30);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", null);
            setPrivateField(fragment, "selectedEndDate", null);
            setPrivateField(fragment, "selectedStartTime", null);
            setPrivateField(fragment, "selectedEndTime", endTime);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
            assertNull(viewModel.getAfterTimeFilter().getValue());
            assertEquals(endTime, viewModel.getBeforeTimeFilter().getValue());
        });
    }

    @Test
    public void invalidTimeOrderShowsErrorAndDoesNotUpdateViewModel() {
        final LocalTime startTime = LocalTime.of(20, 0);
        final LocalTime endTime = LocalTime.of(10, 0);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartTime", startTime);
            setPrivateField(fragment, "selectedEndTime", endTime);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        onView(withId(R.id.tvDateValidationError))
                .check(matches(Matchers.allOf(
                        isDisplayed(), withText(R.string.date_filters_invalid_order))));

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertNull(viewModel.getAfterTimeFilter().getValue());
            assertNull(viewModel.getBeforeTimeFilter().getValue());
        });
    }

    @Test
    public void clearFilterButtonAnchorsAtStartWithMargin() {
        scenario.onFragment(fragment -> {
            final ViewGroup container = (ViewGroup) fragment.requireView()
                    .findViewById(R.id.btnCancelDateFilters)
                    .getParent();

            final View clearButton = findClearButton(container);
            assertEquals(0, container.indexOfChild(clearButton));

            final LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) clearButton.getLayoutParams();
            assertTrue(params.getMarginStart() > 0);
            assertEquals(0, params.getMarginEnd());

            final View spacer = container.getChildAt(1);
            final LinearLayout.LayoutParams spacerParams =
                    (LinearLayout.LayoutParams) spacer.getLayoutParams();
            assertEquals(1f, spacerParams.weight, 0.0001f);
        });
    }

    private void setPrivateField(DateFiltersFragment fragment, String fieldName, Object value) {
        try {
            Field field = DateFiltersFragment.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(fragment, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to set field " + fieldName, e);
        }
    }

    private Object getPrivateField(DateFiltersFragment fragment, String fieldName) {
        try {
            Field field = DateFiltersFragment.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(fragment);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to read field " + fieldName, e);
        }
    }

    private View findClearButton(ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            final View child = container.getChildAt(i);
            if (child instanceof TextView
                    && "Clear Filter".contentEquals(((TextView) child).getText())) {
                return child;
            }
        }
        throw new IllegalStateException("Clear Filter button not found");
    }
}
