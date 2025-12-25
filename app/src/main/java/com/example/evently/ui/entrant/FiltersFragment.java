package com.example.evently.ui.entrant;

import static com.example.evently.utils.DateTimeUtils.toEpochMillis;
import static com.example.evently.utils.DateTimeUtils.toLocalDateTime;
import static com.example.evently.utils.DateTimeUtils.treatAsLocalDate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import com.example.evently.R;
import com.example.evently.data.model.Category;
import com.example.evently.databinding.FragmentFiltersBinding;
import com.example.evently.ui.model.EventFilterViewModel;

/**
 * Fragment for filtering entrant event lists by categories and date/times.
 */
public class FiltersFragment extends DialogFragment {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private FragmentFiltersBinding binding;
    private EventFilterViewModel eventFilterViewModel;

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentFiltersBinding.inflate(getLayoutInflater(), null, false);

        eventFilterViewModel =
                new ViewModelProvider(requireParentFragment()).get(EventFilterViewModel.class);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filter events")
                .setView(binding.getRoot())
                .setNegativeButton(R.string.filters_cancel, (ignored_, ignored) -> {})
                .setPositiveButton(R.string.filters_confirm, (ignored, ignored_) -> {
                    Instant startDateTime = parseTextOrNull(binding.etStartDateTime.getText());
                    Instant endDateTime = parseTextOrNull(binding.etEndDateTime.getText());

                    if (startDateTime != null
                            && endDateTime != null
                            && startDateTime.isAfter(endDateTime)) {
                        showError(R.string.date_filters_invalid_order);
                    } else {
                        eventFilterViewModel.setFilters(
                                collectSelectedCategories(),
                                Optional.ofNullable(startDateTime),
                                Optional.ofNullable(endDateTime));
                    }
                })
                .create();
    }

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventFilterViewModel.getEventFilter().observe(getViewLifecycleOwner(), eventFilter -> {
            final var categories = eventFilter.categories();
            final var startInstantOpt = eventFilter.startTime();
            final var endInstantOpt = eventFilter.endTime();

            // Set up the categories chips.
            final var chipGroup = binding.chipGroupCategories;
            chipGroup.removeAllViews();
            if (!categories.isEmpty()) {
                binding.btnClearFilters.setVisibility(View.VISIBLE);
            }
            for (Category category : Category.values()) {
                final var chip = (Chip) LayoutInflater.from(requireContext())
                        .inflate(R.layout.chip_filter_category, chipGroup, false);
                chip.setText(formatCategoryLabel(category));
                chip.setCheckable(true);
                chip.setChecked(categories.contains(category));
                chip.setTag(category);
                chip.setOnCheckedChangeListener(
                        (ignored, ignored_) -> binding.btnClearFilters.setVisibility(View.VISIBLE));
                chipGroup.addView(chip);
            }

            startInstantOpt.ifPresentOrElse(
                    startInstant -> {
                        binding.etStartDateTime.setText(
                                DATE_TIME_FORMATTER.format(toLocalDateTime(startInstant)));
                        binding.btnClearFilters.setVisibility(View.VISIBLE);
                    },
                    () -> binding.etStartDateTime.setText(""));

            endInstantOpt.ifPresentOrElse(
                    endInstant -> {
                        binding.etEndDateTime.setText(
                                DATE_TIME_FORMATTER.format(toLocalDateTime(endInstant)));
                        binding.btnClearFilters.setVisibility(View.VISIBLE);
                    },
                    () -> binding.etEndDateTime.setText(""));
        });

        binding.etStartDateTime.setOnClickListener(v ->
                setupDateTimePicker(R.string.date_filters_start_title, binding.etStartDateTime));
        binding.etEndDateTime.setOnClickListener(
                v -> setupDateTimePicker(R.string.date_filters_start_title, binding.etEndDateTime));
        binding.btnClearFilters.setOnClickListener(v -> {
            eventFilterViewModel.clearAll();
            binding.btnClearFilters.setVisibility(View.INVISIBLE);
        });
    }

    /**
     * Read the checked chips to build the selected categories set.
     *
     * @return a {@link Set} of selected {@link Category}s.
     */
    private Set<Category> collectSelectedCategories() {
        final var selected = EnumSet.noneOf(Category.class);
        for (int i = 0; i < binding.chipGroupCategories.getChildCount(); i++) {
            final var child = binding.chipGroupCategories.getChildAt(i);
            if (child instanceof Chip chip && chip.isChecked()) {
                selected.add((Category) chip.getTag());
            }
        }
        return selected;
    }

    /**
     * Format a {@link Category} label for display.
     * @param category the category to format.
     * @return the formatted label.
     */
    private String formatCategoryLabel(Category category) {
        final var lower = category.name().toLowerCase(Locale.ROOT);
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    /**
     * Shows the date picker dialog.
     */
    private void setupDateTimePicker(@StringRes int titleRes, TextInputEditText dateTimeBinding) {
        final var pickerBuilder = MaterialDatePicker.Builder.datePicker();
        pickerBuilder.setTitleText(titleRes);
        // Allow only present and future dates.
        final var calendarConstraints = new CalendarConstraints.Builder();
        calendarConstraints.setValidator(DateValidatorPointForward.now());
        pickerBuilder.setCalendarConstraints(calendarConstraints.build());

        // Show the existing selection on the picker.
        final var existingDateTimeTxt = dateTimeBinding.getText();
        Optional<LocalTime> existingTimeTemp = Optional.empty();
        if (!TextUtils.isEmpty(existingDateTimeTxt)) {
            final var existingDateTime =
                    LocalDateTime.parse(existingDateTimeTxt, DATE_TIME_FORMATTER);
            existingTimeTemp = Optional.of(existingDateTime.toLocalTime());
            pickerBuilder.setSelection(toEpochMillis(existingDateTime.toLocalDate()));
        }
        final var existingTimeOpt = existingTimeTemp;

        final var picker = pickerBuilder.build();
        picker.addOnPositiveButtonClickListener(dateSelection -> {
            if (dateSelection != null) {
                final var timePickerBuilder =
                        new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H);

                // Show any existing selection on the picker.
                existingTimeOpt.ifPresent(existingTime -> {
                    timePickerBuilder.setHour(existingTime.getHour());
                    timePickerBuilder.setMinute(existingTime.getMinute());
                });

                final var timePicker = timePickerBuilder.build();

                timePicker.addOnPositiveButtonClickListener(ignored -> {
                    clearErrors();
                    binding.btnClearFilters.setVisibility(View.VISIBLE);
                    final var timeSelection =
                            LocalTime.of(timePicker.getHour(), timePicker.getMinute());
                    final var selectedDateTime =
                            treatAsLocalDate(dateSelection).atTime(timeSelection);
                    dateTimeBinding.setText(DATE_TIME_FORMATTER.format(selectedDateTime));
                });

                timePicker.show(getParentFragmentManager(), "filter_date_time_inner_picker");
            }
        });

        picker.show(getParentFragmentManager(), "filter_date_time_picker");
    }

    /**
     * Parse a given datetime text if present, or return null.
     * @param inpText DateTime field text.
     * @return Parsed local date time in UTC.
     */
    private Instant parseTextOrNull(CharSequence inpText) {
        if (TextUtils.isEmpty(inpText)) {
            return null;
        }
        return LocalDateTime.parse(inpText, DATE_TIME_FORMATTER)
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }

    /**
     * Shows an error message in the UI.
     *
     * @param stringId The string resource ID for the error message to display.
     */
    private void showError(@StringRes int stringId) {
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
     * Cleans up the binding object when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
