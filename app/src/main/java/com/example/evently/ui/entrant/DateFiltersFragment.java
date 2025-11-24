package com.example.evently.ui.entrant;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

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

        binding.btnCancelDateFilters.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnConfirmDateFilters.setOnClickListener(v -> {
            if (applyFiltersFromInput()) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    /**
     * Prefills the date inputs with any active filters so users can edit or clear them.
     */
    private void populateExistingFilters() {
        final var afterDate = eventsViewModel.getAfterDateFilter().getValue();
        final var beforeDate = eventsViewModel.getBeforeDateFilter().getValue();

        if (afterDate != null) {
            binding.etAfterDate.setText(DATE_FORMATTER.format(afterDate));
        }
        if (beforeDate != null) {
            binding.etBeforeDate.setText(DATE_FORMATTER.format(beforeDate));
        }
    }

    /**
     * Parses user-entered dates, validates ordering, and saves them to the view model.
     *
     * @return {@code true} if input is valid and filters were applied; {@code false} otherwise.
     */
    private boolean applyFiltersFromInput() {
        final var afterInput = binding.etAfterDate.getText();
        final var beforeInput = binding.etBeforeDate.getText();

        clearErrors();

        final LocalDate afterDate;
        final LocalDate beforeDate;
        try {
            afterDate = parseDate(afterInput == null ? null : afterInput.toString());
        } catch (DateTimeParseException e) {
            binding.tilAfterDate.setError("Use format yyyy/MM/dd");
            return false;
        }

        try {
            beforeDate = parseDate(beforeInput == null ? null : beforeInput.toString());
        } catch (DateTimeParseException e) {
            binding.tilBeforeDate.setError("Use format yyyy/MM/dd");
            return false;
        }

        if (afterDate != null && beforeDate != null && afterDate.isAfter(beforeDate)) {
            binding.tilBeforeDate.setError("Before Date must be after After Date");
            return false;
        }

        eventsViewModel.setDateFilters(afterDate, beforeDate);
        return true;
    }

    /**
     * Clears any validation errors on the date input text fields.
     */
    private void clearErrors() {
        binding.tilAfterDate.setError(null);
        binding.tilBeforeDate.setError(null);
    }

    /**
     * Converts a string input into a {@link LocalDate} using the screen's formatter.
     * @param input user-entered date text, expected in {@code yyyy/MM/dd} format.
     * @return parsed {@link LocalDate} when non-empty; {@code null} if the field was blank.
     */
    private LocalDate parseDate(@Nullable String input) {
        if (TextUtils.isEmpty(input)) {
            return null;
        }

        return LocalDate.parse(input.trim(), DATE_FORMATTER);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
