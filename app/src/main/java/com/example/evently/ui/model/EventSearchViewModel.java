package com.example.evently.ui.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evently.ui.common.SearchEventsFragment;
import com.example.evently.ui.common.SearchedEventsFragment;

/**
 * A view model to continually update and track a search string.
 * @see SearchEventsFragment
 * @see SearchedEventsFragment
 */
public class EventSearchViewModel extends ViewModel {
    private final MutableLiveData<String> searchStringLive = new MutableLiveData<>("");

    /**
     * @return The event filter live data to observe changes upon.
     */
    public LiveData<String> getSearchString() {
        return searchStringLive;
    }

    /**
     * Set the given search string.
     * @param newSearchString Search string to search the events by.
     */
    public void setSearchString(String newSearchString) {
        searchStringLive.setValue(newSearchString);
    }

    /**
     * Clear all filters.
     */
    public void clear() {
        searchStringLive.setValue("");
    }
}
