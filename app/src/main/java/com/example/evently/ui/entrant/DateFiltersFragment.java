package com.example.evently.ui.entrant;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.datepicker.MaterialDatePicker;

import com.example.evently.R;
import com.example.evently.databinding.FragmentDateFiltersBinding;
import com.example.evently.ui.model.BrowseEventsViewModel;

/**
 * Fragment for filtering entrant event lists by date.
 */
public class DateFiltersFragment extends Fragment {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private FragmentDateFiltersBinding binding;
    private BrowseEventsViewModel eventsViewModel;

    @Nullable private LocalDate selectedStartDate;

    @Nullable private LocalDate selectedEndDate;

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
        binding.tilEndDate.setEndIconOnClickListener(v -> clearEndDate());

        View.OnClickListener startPickerListener = v -> showDatePicker(true);
        binding.tilStartDate.setOnClickListener(startPickerListener);
        binding.etStartDate.setOnClickListener(startPickerListener);

        View.OnClickListener endPickerListener = v -> showDatePicker(false);
        binding.tilEndDate.setOnClickListener(endPickerListener);
        binding.etEndDate.setOnClickListener(endPickerListener);

        binding.btnCancelDateFilters.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnConfirmDateFilters.setOnClickListener(v -> {
            if (applyFiltersFromSelection()) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    /**
     * Prefills the date picker state with any active filters so users can edit or clear them.
     */
    private void populateExistingFilters() {
        selectedStartDate = eventsViewModel.getAfterDateFilter().getValue();
        selectedEndDate = eventsViewModel.getBeforeDateFilter().getValue();

        renderSelectedDates();
    }

    /**
     * Applies the selected dates, validates ordering, and saves them to the view model.
     *
     * @return {@code true} if input is valid and filters were applied; {@code false} otherwise.
     */
    private boolean applyFiltersFromSelection() {
        clearErrors();

        if (selectedStartDate == null && selectedEndDate == null) {
            eventsViewModel.setDateFilters(null, null);
            return true;
        }

        if (selectedStartDate != null
                && selectedEndDate != null
                && selectedStartDate.isAfter(selectedEndDate)) {
            showError(R.string.date_filters_invalid_order);
            return false;
        }

        eventsViewModel.setDateFilters(selectedStartDate, selectedEndDate);
        return true;
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
     * Renders the selected dates in the UI.
     */
    private void renderSelectedDates() {
        binding.etStartDate.setText(
                selectedStartDate == null ? null : DATE_FORMATTER.format(selectedStartDate));
        binding.etEndDate.setText(
                selectedEndDate == null ? null : DATE_FORMATTER.format(selectedEndDate));
    }

    /**
     * Clears the selected start date.
     */
    private void clearStartDate() {
        selectedStartDate = null;
        binding.etStartDate.setText(null);
        clearErrors();
    }

    /**
     * Clears the selected end date.
     */
    private void clearEndDate() {
        selectedEndDate = null;
        binding.etEndDate.setText(null);
        clearErrors();
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
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Converts a local date to an epoch millis.
     * @param localDate The local date to convert.
     * @return The converted epoch millis.
     */
    private long toEpochMillis(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
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
