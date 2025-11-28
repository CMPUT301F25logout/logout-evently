package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import android.view.View;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.ui.entrant.DateFiltersFragment;
import com.example.evently.ui.model.BrowseEventsViewModel;

/**
 * UI tests for {@link DateFiltersFragment} filter behavior.
 */
@RunWith(AndroidJUnit4.class)
public class DateFiltersTest extends EmulatedFragmentTest<DateFiltersFragment> {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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
    public void confirmValidRange_updatesViewModelFilters() {
        final LocalDate startDate = LocalDate.of(2030, 1, 1);
        final LocalDate endDate = LocalDate.of(2030, 1, 2);
        final LocalTime startTime = LocalTime.of(9, 0);
        final LocalTime endTime = LocalTime.of(17, 30);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", startDate);
            setPrivateField(fragment, "selectedEndDate", endDate);
            setPrivateField(fragment, "selectedStartTime", startTime);
            setPrivateField(fragment, "selectedEndTime", endTime);

            ((TextView) fragment.requireView().findViewById(R.id.etStartDate))
                    .setText(DATE_FORMATTER.format(startDate));
            ((TextView) fragment.requireView().findViewById(R.id.etEndDate))
                    .setText(DATE_FORMATTER.format(endDate));
            ((TextView) fragment.requireView().findViewById(R.id.etStartTime))
                    .setText(TIME_FORMATTER.format(startTime));
            ((TextView) fragment.requireView().findViewById(R.id.etEndTime))
                    .setText(TIME_FORMATTER.format(endTime));
        });

        scenario.onFragment(fragment ->
                fragment.requireView().findViewById(R.id.btnConfirmDateFilters).performClick());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);

            assertEquals(
                    LocalDateTime.of(startDate, LocalTime.MIDNIGHT),
                    viewModel.getAfterDateFilter().getValue());
            assertEquals(
                    LocalDateTime.of(endDate, LocalTime.MIDNIGHT),
                    viewModel.getBeforeDateFilter().getValue());
            assertEquals(startTime, viewModel.getAfterTimeFilter().getValue());
            assertEquals(endTime, viewModel.getBeforeTimeFilter().getValue());
        });
    }

    @Test
    public void invalidOrdering_showsErrorAndDoesNotUpdateFilters() {
        final LocalDate startDate = LocalDate.of(2030, 2, 1);
        final LocalDate endDate = LocalDate.of(2030, 1, 1);
        final LocalTime startTime = LocalTime.of(18, 0);
        final LocalTime endTime = LocalTime.of(9, 0);

        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectedStartDate", startDate);
            setPrivateField(fragment, "selectedEndDate", endDate);
            setPrivateField(fragment, "selectedStartTime", startTime);
            setPrivateField(fragment, "selectedEndTime", endTime);

            ((TextView) fragment.requireView().findViewById(R.id.etStartDate))
                    .setText(DATE_FORMATTER.format(startDate));
            ((TextView) fragment.requireView().findViewById(R.id.etEndDate))
                    .setText(DATE_FORMATTER.format(endDate));
            ((TextView) fragment.requireView().findViewById(R.id.etStartTime))
                    .setText(TIME_FORMATTER.format(startTime));
            ((TextView) fragment.requireView().findViewById(R.id.etEndTime))
                    .setText(TIME_FORMATTER.format(endTime));
        });

        scenario.onFragment(fragment ->
                fragment.requireView().findViewById(R.id.btnConfirmDateFilters).performClick());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);

            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
            assertNull(viewModel.getAfterTimeFilter().getValue());
            assertNull(viewModel.getBeforeTimeFilter().getValue());

            final TextView error = fragment.requireView().findViewById(R.id.tvDateValidationError);
            assertEquals(View.VISIBLE, error.getVisibility());
            assertEquals(
                    fragment.getString(R.string.date_filters_invalid_order),
                    error.getText().toString());
        });
    }

    @Test
    public void clearFiltersButton_resetsFieldsAndViewModel() {
        final LocalDate existingStartDate = LocalDate.of(2030, 3, 10);
        final LocalDate existingEndDate = LocalDate.of(2030, 3, 20);
        final LocalTime existingStartTime = LocalTime.of(10, 0);
        final LocalTime existingEndTime = LocalTime.of(15, 0);

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            viewModel.setDateFilters(
                    LocalDateTime.of(existingStartDate, LocalTime.MIDNIGHT),
                    LocalDateTime.of(existingEndDate, LocalTime.MIDNIGHT),
                    existingStartTime,
                    existingEndTime);

            setPrivateField(fragment, "selectedStartDate", existingStartDate);
            setPrivateField(fragment, "selectedEndDate", existingEndDate);
            setPrivateField(fragment, "selectedStartTime", existingStartTime);
            setPrivateField(fragment, "selectedEndTime", existingEndTime);

            ((TextView) fragment.requireView().findViewById(R.id.etStartDate))
                    .setText(DATE_FORMATTER.format(existingStartDate));
            ((TextView) fragment.requireView().findViewById(R.id.etEndDate))
                    .setText(DATE_FORMATTER.format(existingEndDate));
            ((TextView) fragment.requireView().findViewById(R.id.etStartTime))
                    .setText(TIME_FORMATTER.format(existingStartTime));
            ((TextView) fragment.requireView().findViewById(R.id.etEndTime))
                    .setText(TIME_FORMATTER.format(existingEndTime));
        });

        onView(withText("Clear Filter")).perform(click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);

            assertNull(viewModel.getAfterDateFilter().getValue());
            assertNull(viewModel.getBeforeDateFilter().getValue());
            assertNull(viewModel.getAfterTimeFilter().getValue());
            assertNull(viewModel.getBeforeTimeFilter().getValue());

            assertEquals(
                    "",
                    ((TextView) fragment.requireView().findViewById(R.id.etStartDate))
                            .getText()
                            .toString());
            assertEquals(
                    "",
                    ((TextView) fragment.requireView().findViewById(R.id.etEndDate))
                            .getText()
                            .toString());
            assertEquals(
                    "",
                    ((TextView) fragment.requireView().findViewById(R.id.etStartTime))
                            .getText()
                            .toString());
            assertEquals(
                    "",
                    ((TextView) fragment.requireView().findViewById(R.id.etEndTime))
                            .getText()
                            .toString());
        });
    }

    private void setPrivateField(DateFiltersFragment fragment, String fieldName, Object value) {
        try {
            final Field field = DateFiltersFragment.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(fragment, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to set field " + fieldName, e);
        }
    }
}
