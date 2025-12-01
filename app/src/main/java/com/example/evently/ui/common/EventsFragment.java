package com.example.evently.ui.common;

import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.evently.data.model.Event;

/**
 * A reusable abstract fragment representing a list of Events.
 * This is meant to serve as the template for all the "browse events" screens,
 * e.g: Browse all events, Browse own events etc.
 * <p>
 * Extending classes can provide initial list of events by implementing `initEvents`.
 * Extending classes will also have access to the {@link EventRecyclerViewAdapter} to modify dynamically.
 * @see EventRecyclerViewAdapter
 */
public abstract class EventsFragment extends LiveEventsFragment<Void> {

    /**
     * Adapter to manage the events list dynamically.
     */
    protected EventRecyclerViewAdapter adapter;

    private final MutableLiveData<Void> trivial = new MutableLiveData<>();

    @Override
    protected LiveData<Void> getLiveData() {
        return trivial;
    }

    @Override
    protected void updateEventsBy(Void target, Consumer<List<Event>> act) {
        initEvents(act);
    }

    /**
     * This method will be called by onCreateView to set up the events view.
     * It is guaranteed that the activity context will be available at the time of calling.
     * @param callback Callback that will be passed the events into.
     */
    protected abstract void initEvents(Consumer<List<Event>> callback);

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        trivial.setValue(null);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
