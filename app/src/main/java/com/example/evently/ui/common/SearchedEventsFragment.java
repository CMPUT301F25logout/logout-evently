package com.example.evently.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.evently.ui.model.EventSearchViewModel;

/**
 * A fragment representing list of events filtered by the search bar
 */
public abstract class SearchedEventsFragment extends LiveEventsFragment<String> {
    private EventSearchViewModel eventSearchViewModel;

    @Override
    protected LiveData<String> getLiveData() {
        return eventSearchViewModel.getSearchString();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        eventSearchViewModel =
                new ViewModelProvider(requireParentFragment()).get(EventSearchViewModel.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
