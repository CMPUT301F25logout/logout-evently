package com.example.evently.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.evently.R;
import com.example.evently.databinding.FragmentSearchEventsBinding;
import com.example.evently.ui.model.EventSearchViewModel;

/**
 * Fragment that hosts an event search bar and a child fragment displaying a filtered list of events.
 * <p>
 * This fragment manages a {@link SearchView} and forwards text changes to
 * {@link SearchedEventsFragment} so the list updates in real time based on the search query.
 */
public abstract class SearchEventsFragment<F extends SearchedEventsFragment> extends Fragment {
    private FragmentSearchEventsBinding binding;

    private EventSearchViewModel eventSearchViewModel;

    protected abstract Class<F> getSearchedEventsFragment();

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchEventsBinding.inflate(inflater, container, false);
        eventSearchViewModel = new ViewModelProvider(this).get(EventSearchViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.searchedEventsContainer, getSearchedEventsFragment(), null)
                    .commit();
        }

        SearchView searchView = binding.eventSearch;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                eventSearchViewModel.setSearchString(newText);
                return false;
            }

            // Don't need to do anything here since we update search on demand.
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
