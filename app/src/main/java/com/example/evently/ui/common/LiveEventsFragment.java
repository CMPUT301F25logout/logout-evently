package com.example.evently.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
import com.example.evently.data.model.Event;

/**
 * Fragment base class that renders a live-updating list of {@link Event} objects.
 * <p>
 * Subclasses supply a {@link LiveData} source and refresh trigger, and provide
 * per-item click handling via {@link #onEventClick(Event)}.
 */
public abstract class LiveEventsFragment<T> extends Fragment {

    /**
     *
     * @return the {@link LiveData} source for events to render.
     */
    protected abstract LiveData<T> getLiveData();

    /**
     * Updates the list of events based on the latest observed data.
     *
     * @param target new value from {@link #getLiveData()}.
     * @param act callback to receive the updated {@link Event} list.
     */
    protected abstract void updateEventsBy(T target, Consumer<List<Event>> act);

    /**
     * Listener to attach to the event on click.
     * This may be different for the entrant vs organizer event click.
     * @param event The structural representation of the Event view that was clicked.
     */
    protected abstract void onEventClick(Event event);

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView =
                (RecyclerView) inflater.inflate(R.layout.fragment_event_list, container, false);

        if (recyclerView == null) {
            throw new AssertionError("EventsFragment.onCreateView called with non RecyclerView");
        }

        recyclerView.setAdapter(
                new EventRecyclerViewAdapter(new ArrayList<>(), this::onEventClick));

        getLiveData().observe(getViewLifecycleOwner(), target -> {
            updateEventsBy(target, events -> {
                final var adapter = new EventRecyclerViewAdapter(events, this::onEventClick);
                recyclerView.swapAdapter(adapter, false);
            });
        });

        return recyclerView;
    }
}
