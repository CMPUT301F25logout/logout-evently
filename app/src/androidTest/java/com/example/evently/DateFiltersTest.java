package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.time.LocalDate;

import android.view.View;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.ui.entrant.DateFiltersFragment;
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
        final LocalDate endDate = LocalDate.of(2030, 1, 10);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", startDate);
            setPrivateField(fragment, "selectedEndDate", endDate);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertEquals(startDate, viewModel.getAfterDateFilter().getValue());
            assertEquals(endDate, viewModel.getBeforeDateFilter().getValue());

            TextView errorView = fragment.requireView().findViewById(R.id.tvDateValidationError);
            assertEquals(View.GONE, errorView.getVisibility());
        });
    }

    @Test
    public void invalidDateOrderShowsErrorAndDoesNotUpdateViewModel() {
        final LocalDate startDate = LocalDate.of(2030, 2, 1);
        final LocalDate endDate = LocalDate.of(2030, 1, 1);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", startDate);
            setPrivateField(fragment, "selectedEndDate", endDate);
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
        });
    }

    @Test
    public void confirmClearsFiltersWhenNoDatesSelected() {
        final LocalDate previousStart = LocalDate.of(2029, 5, 1);
        final LocalDate previousEnd = LocalDate.of(2029, 6, 1);

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            viewModel.setDateFilters(previousStart, previousEnd);

            setPrivateField(fragment, "selectedStartDate", null);
            setPrivateField(fragment, "selectedEndDate", null);
        });

        onView(withId(R.id.btnConfirmDateFilters)).perform(scrollTo(), click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
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
}
