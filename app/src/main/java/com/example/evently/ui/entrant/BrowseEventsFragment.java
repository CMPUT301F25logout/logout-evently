package com.example.evently.ui.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.evently.databinding.FragmentBrowseEventsBinding;
import com.example.evently.ui.model.EventFilterViewModel;

/**
 * A fragment representing a list of events the Entrant can join
 */
public class BrowseEventsFragment extends Fragment {
    private FragmentBrowseEventsBinding binding;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentBrowseEventsBinding.inflate(getLayoutInflater(), container, false);
        EventFilterViewModel ignored = new ViewModelProvider(this).get(EventFilterViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnBrowseFilters.setOnClickListener(
                ignored -> new FiltersFragment().show(getChildFragmentManager(), "filters_dialog"));
    }
}
