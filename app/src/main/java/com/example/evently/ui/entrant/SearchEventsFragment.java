package com.example.evently.ui.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.evently.R;
import com.example.evently.databinding.FragmentEventSearchBinding;

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

        // Check if child fragment exists
        listFragment = (SearchEventsListFragment) getChildFragmentManager().findFragmentByTag("searchListTag");

        if (savedInstanceState == null && listFragment == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.eventListContainer, SearchEventsListFragment.class, null, "searchListTag")
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
