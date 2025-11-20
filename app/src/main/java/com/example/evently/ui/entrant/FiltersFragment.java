package com.example.evently.ui.entrant;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.chip.Chip;

import com.example.evently.R;
import com.example.evently.data.model.Category;
import com.example.evently.databinding.FragmentFiltersBinding;
import com.example.evently.ui.model.EntrantEventsViewModel;

/**
 * Fragment for filtering entrant event lists by {@link Category}.
 */
public class FiltersFragment extends Fragment {
    private FragmentFiltersBinding binding;
    private EntrantEventsViewModel eventsViewModel;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentFiltersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventsViewModel =
                new ViewModelProvider(requireActivity()).get(EntrantEventsViewModel.class);

        setupCategoryChips();

        binding.btnCancelFilters.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnConfirmFilters.setOnClickListener(v -> {
            eventsViewModel.setSelectedCategories(collectSelectedCategories());
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    /**
     * Inflate and configure the category chips to reflect the currently selected filters.
     */
    private void setupCategoryChips() {
        final var chipGroup = binding.chipGroupCategories;
        chipGroup.removeAllViews();
        final var selectedCategories = eventsViewModel.getSelectedCategories().getValue();

        for (Category category : Category.values()) {
            final var chip = (Chip) LayoutInflater.from(requireContext())
                    .inflate(R.layout.chip_filter_category, chipGroup, false);
            chip.setText(formatCategoryLabel(category));
            chip.setCheckable(true);
            chip.setChecked(selectedCategories != null && selectedCategories.contains(category));
            chip.setTag(category);
            chipGroup.addView(chip);
        }
    }

    /**
     * Read the checked chips to build the selected categories set.
     *
     * @return a {@link Set} of selected {@link Category}s.
     */
    private Set<Category> collectSelectedCategories() {
        final var selected = EnumSet.noneOf(Category.class);
        for (int i = 0; i < binding.chipGroupCategories.getChildCount(); i++) {
            View child = binding.chipGroupCategories.getChildAt(i);
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
     * Clear the view binding reference when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
