package com.example.evently.ui.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.evently.R;
import com.example.evently.databinding.FragmentEventSearchBinding;

/**
 * Fragment that hosts an event search bar and a child fragment displaying a filtered list of events.
 * <p>
 * This fragment manages a {@link SearchView} and forwards text changes to
 * {@link SearchEventsListFragment} so the list updates in real time based on the search query.
 */
public class SearchEventsFragment extends Fragment {
    private FragmentEventSearchBinding binding;
    private SearchEventsListFragment listFragment;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEventSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchView searchView = binding.eventSearch;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Handles the search submit action. Currently unused.
             *
             * @param query The submitted search text.
             * @return false to allow default handling.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            /**
             * Called each time the user modifies the search text.
             * Sends the new text to the child list fragment for filtering.
             *
             * @param newText Updated text entered by the user.
             * @return false to allow default handling.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                if (listFragment != null) {
                    listFragment.filter(newText);
                }
                return false;
            }
        });

        // Attempt to retrieve existing child fragment
        listFragment = (SearchEventsListFragment)
                getChildFragmentManager().findFragmentByTag("searchListTag");

        // Create if not found
        if (listFragment == null) {
            listFragment = new SearchEventsListFragment();

            getChildFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.eventListContainer, listFragment, "searchListTag")
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
