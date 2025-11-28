package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.model.Category;
import com.example.evently.ui.entrant.CategoryFiltersFragment;
import com.example.evently.ui.model.BrowseEventsViewModel;

/**
 * UI tests for {@link CategoryFiltersFragment}.
 */
@RunWith(AndroidJUnit4.class)
public class CategoryFiltersTest extends EmulatedFragmentTest<CategoryFiltersFragment> {

    @Override
    protected int getGraph() {
        return R.navigation.entrant_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_filters;
    }

    @Override
    protected Class<CategoryFiltersFragment> getFragmentClass() {
        return CategoryFiltersFragment.class;
    }

    @Test
    public void displaysAllCategoriesWithFormattedLabels() {
        scenario.onFragment(fragment -> {
            final var chipGroup =
                    (ChipGroup) fragment.requireView().findViewById(R.id.chipGroupCategories);
            assertEquals(Category.values().length, chipGroup.getChildCount());

            final Set<String> expectedLabels = Arrays.stream(Category.values())
                    .map(this::formatLabel)
                    .collect(Collectors.toSet());

            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                assertTrue(child instanceof Chip);
                final var chip = (Chip) child;
                assertTrue(expectedLabels.contains(chip.getText().toString()));
            }
        });
    }

    @Test
    public void selectingCategoriesUpdatesViewModel() {
        onView(withText("Sports")).perform(click());
        onView(withText("Social")).perform(click());

        onView(withId(R.id.btnConfirmFilters)).perform(scrollTo(), click());

        scenario.onFragment(fragment -> {
            final var viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(BrowseEventsViewModel.class);
            final var selected = viewModel.getSelectedCategories().getValue();
            final var expected = Set.of(Category.SPORTS, Category.SOCIAL);
            assertEquals(expected, selected);
        });
    }

    private String formatLabel(Category category) {
        final var lower = category.name().toLowerCase(Locale.ROOT);
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }
}
