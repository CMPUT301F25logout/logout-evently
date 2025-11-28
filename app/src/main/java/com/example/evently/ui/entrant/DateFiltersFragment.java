package com.example.evently.ui.entrant;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import com.example.evently.R;
import com.example.evently.databinding.FragmentDateFiltersBinding;
import com.example.evently.ui.model.BrowseEventsViewModel;

/**
 * Fragment for filtering entrant event lists by date and optional time boundaries.
 */
public class DateFiltersFragment extends Fragment {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private FragmentDateFiltersBinding binding;
    private BrowseEventsViewModel eventsViewModel;

    @Nullable private LocalDate selectedStartDate;

    @Nullable private LocalTime selectedStartTime;

    @Nullable private LocalDate selectedEndDate;

    @Nullable private LocalTime selectedEndTime;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentDateFiltersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventsViewModel = new ViewModelProvider(requireActivity()).get(BrowseEventsViewModel.class);

        populateExistingFilters();

        binding.tilStartDate.setEndIconOnClickListener(v -> clearStartDate());
        binding.tilStartTime.setEndIconOnClickListener(v -> clearStartTime());
        binding.tilEndDate.setEndIconOnClickListener(v -> clearEndDate());
        binding.tilEndTime.setEndIconOnClickListener(v -> clearEndTime());

        View.OnClickListener startPickerListener = v -> showDatePicker(true);
        binding.tilStartDate.setOnClickListener(startPickerListener);
        binding.etStartDate.setOnClickListener(startPickerListener);

        View.OnClickListener startTimePickerListener = v -> showTimePicker(true);
        binding.tilStartTime.setOnClickListener(startTimePickerListener);
        binding.etStartTime.setOnClickListener(startTimePickerListener);

        View.OnClickListener endPickerListener = v -> showDatePicker(false);
        binding.tilEndDate.setOnClickListener(endPickerListener);
        binding.etEndDate.setOnClickListener(endPickerListener);

        View.OnClickListener endTimePickerListener = v -> showTimePicker(false);
        binding.tilEndTime.setOnClickListener(endTimePickerListener);
        binding.etEndTime.setOnClickListener(endTimePickerListener);

        binding.btnCancelDateFilters.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnConfirmDateFilters.setOnClickListener(v -> {
            if (applyFiltersFromSelection()) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });

        insertClearFiltersButton();
    }

    /**
     * Prefills the date picker state with any active filters so users can edit or clear them.
     */
    private void populateExistingFilters() {
        final LocalDateTime startFilter = eventsViewModel.getAfterDateFilter().getValue();
        final LocalDateTime endFilter = eventsViewModel.getBeforeDateFilter().getValue();
        final LocalTime startTimeFilter = eventsViewModel.getAfterTimeFilter().getValue();
        final LocalTime endTimeFilter = eventsViewModel.getBeforeTimeFilter().getValue();

        if (startFilter != null) {
            selectedStartDate = startFilter.toLocalDate();
        }
        if (startTimeFilter != null) {
            selectedStartTime = startTimeFilter;
        }
        if (endFilter != null) {
            selectedEndDate = endFilter.toLocalDate();
        }
        if (endTimeFilter != null) {
            selectedEndTime = endTimeFilter;
        }

        renderSelectedDates();
    }

    /**
     * Dynamically inserts a "Clear Filter" button to reset date/time selections.
     */
    private void insertClearFiltersButton() {
        final var buttonsContainer = (ViewGroup) binding.btnCancelDateFilters.getParent();
        if (!(buttonsContainer instanceof LinearLayout)) {
            return;
        }

        ((LinearLayout) buttonsContainer).setGravity(Gravity.CENTER_VERTICAL);

        final var cancelParams = binding.btnCancelDateFilters.getLayoutParams();
        final var newButtonParams = cancelParams instanceof LinearLayout.LayoutParams
                ? new LinearLayout.LayoutParams((LinearLayout.LayoutParams) cancelParams)
                : new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final int startMarginPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        newButtonParams.setMarginStart(startMarginPx);
        newButtonParams.setMarginEnd(0);

        final var clearButton = new MaterialButton(
                requireContext(),
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        clearButton.setLayoutParams(newButtonParams);
        clearButton.setText("Clear Filter");
        clearButton.setOnClickListener(v -> resetFiltersToDefault());

        final int cancelIndex = buttonsContainer.indexOfChild(binding.btnCancelDateFilters);
        buttonsContainer.addView(clearButton, Math.max(0, cancelIndex));

        final var spacerParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        spacerParams.setMarginEnd(0);
        buttonsContainer.addView(
                new View(requireContext()), Math.max(1, cancelIndex + 1), spacerParams);
    }

    /**
     * Applies the selected dates, validates ordering, and saves them to the view model.
     *
     * @return {@code true} if input is valid and filters were applied; {@code false} otherwise.
     */
    private boolean applyFiltersFromSelection() {
        clearErrors();

        final LocalDateTime startDateTime = selectedStartDate == null
                ? null
                : LocalDateTime.of(selectedStartDate, LocalTime.MIDNIGHT);
        final LocalDateTime endDateTime = selectedEndDate == null
                ? null
                : LocalDateTime.of(selectedEndDate, LocalTime.MIDNIGHT);

        if (startDateTime == null
                && endDateTime == null
                && selectedStartTime == null
                && selectedEndTime == null) {
            eventsViewModel.setDateFilters(null, null, null, null);
            return true;
        }

        if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
            showError(R.string.date_filters_invalid_order);
            return false;
        }

        if (selectedStartTime != null
                && selectedEndTime != null
                && selectedStartTime.isAfter(selectedEndTime)) {
            showError(R.string.date_filters_invalid_order);
            return false;
        }

        eventsViewModel.setDateFilters(
                startDateTime, endDateTime, selectedStartTime, selectedEndTime);
        return true;
    }

    /**
     * Clears any selected dates/times and resets the view-model filters.
     */
    private void resetFiltersToDefault() {
        selectedStartDate = null;
        selectedStartTime = null;
        selectedEndDate = null;
        selectedEndTime = null;

        renderSelectedDates();
        clearErrors();
        eventsViewModel.setDateFilters(null, null, null, null);
    }

    /**
     * Shows the date picker dialog.
     *
     * @param isStartDate {@code true} if the start date is being selected; {@code false} otherwise.
     */
    private void showDatePicker(boolean isStartDate) {
        final var pickerBuilder = MaterialDatePicker.Builder.datePicker();
        pickerBuilder.setTitleText(
                isStartDate
                        ? R.string.date_filters_picker_start_title
                        : R.string.date_filters_picker_end_title);

        final LocalDate existingDate = isStartDate ? selectedStartDate : selectedEndDate;
        if (existingDate != null) {
            pickerBuilder.setSelection(toEpochMillis(existingDate));
        }

        final var picker = pickerBuilder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                if (isStartDate) {
                    selectedStartDate = toLocalDate(selection);
                } else {
                    selectedEndDate = toLocalDate(selection);
                }
                renderSelectedDates();
                clearErrors();
            }
        });

        picker.show(
                getParentFragmentManager(), isStartDate ? "start_date_picker" : "end_date_picker");
    }

    /**
     * Shows the time picker dialog.
     *
     * @param isStartTime {@code true} if the start time is being selected; {@code false} otherwise.
     */
    private void showTimePicker(boolean isStartTime) {
        final var builder = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H);

        final LocalTime existingTime = isStartTime ? selectedStartTime : selectedEndTime;
        if (existingTime != null) {
            builder.setHour(existingTime.getHour());
            builder.setMinute(existingTime.getMinute());
        }

        final var picker = builder.build();
        picker.addOnPositiveButtonClickListener(_selection -> {
            if (isStartTime) {
                selectedStartTime = LocalTime.of(picker.getHour(), picker.getMinute());
            } else {
                selectedEndTime = LocalTime.of(picker.getHour(), picker.getMinute());
            }
            renderSelectedTimes();
            clearErrors();
        });

        picker.show(
                getParentFragmentManager(), isStartTime ? "start_time_picker" : "end_time_picker");
    }

    /**
     * Renders the selected dates in the UI.
     */
    private void renderSelectedDates() {
        binding.etStartDate.setText(
                selectedStartDate == null ? null : DATE_FORMATTER.format(selectedStartDate));
        binding.etEndDate.setText(
                selectedEndDate == null ? null : DATE_FORMATTER.format(selectedEndDate));

        renderSelectedTimes();
    }

    /**
     * Renders the selected times in the UI.
     */
    private void renderSelectedTimes() {
        binding.etStartTime.setText(
                selectedStartTime == null ? null : TIME_FORMATTER.format(selectedStartTime));
        binding.etEndTime.setText(
                selectedEndTime == null ? null : TIME_FORMATTER.format(selectedEndTime));
    }

    /**
     * Clears the selected start date.
     */
    private void clearStartDate() {
        selectedStartDate = null;
        binding.etStartDate.setText(null);
        clearStartTime();
        clearErrors();
    }

    /**
     * Clears the selected start time.
     */
    private void clearStartTime() {
        selectedStartTime = null;
        binding.etStartTime.setText(null);
    }

    /**
     * Clears the selected end date.
     */
    private void clearEndDate() {
        selectedEndDate = null;
        binding.etEndDate.setText(null);
        clearEndTime();
        clearErrors();
    }

    /**
     * Clears the selected end time.
     */
    private void clearEndTime() {
        selectedEndTime = null;
        binding.etEndTime.setText(null);
    }

    /**
     * Shows an error message in the UI.
     *
     * @param stringId The string resource ID for the error message to display.
     */
    private void showError(int stringId) {
        binding.tvDateValidationError.setText(stringId);
        binding.tvDateValidationError.setVisibility(View.VISIBLE);
    }

    /**
     * Clears any error messages in the UI.
     */
    private void clearErrors() {
        binding.tvDateValidationError.setText(null);
        binding.tvDateValidationError.setVisibility(View.GONE);
    }

    /**
     * Converts an epoch millis to a local date.
     * @param epochMillis The epoch millis to convert.
     * @return The converted local date.
     */
    private LocalDate toLocalDate(long epochMillis) {
        final Instant instant = Instant.ofEpochMilli(epochMillis);
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    /**
     * Converts a local date to an epoch millis.
     * @param localDate The local date to convert.
     * @return The converted epoch millis.
     */
    private long toEpochMillis(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    /**
     * Cleans up the binding object when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
