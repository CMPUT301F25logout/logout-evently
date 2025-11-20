package com.example.evently.ui.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
import com.example.evently.data.model.Event;

/**
 * Fragment base class that renders a live-updating list of {@link Event} objects.
 * <p>
 * Subclasses supply a {@link LiveData} source and refresh trigger, and provide
 * per-item click handling via {@link #onEventClick(Event)}.
 */
public abstract class LiveEventsFragment extends Fragment {

    /**
     * Returns the {@link LiveData} source for events to render.
     */
    protected abstract LiveData<List<Event>> getEventsLiveData();

    /**
     * Called when the fragment is ready to request a data refresh.
     */
    protected abstract void requestRefresh();

    /**
     * Handles clicks on an {@link Event} row.
     *
     * @param event clicked event.
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
            throw new AssertionError(
                    "LiveEventsFragment.onCreateView called with non RecyclerView");
        }

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(
                new EventRecyclerViewAdapter(new ArrayList<>(), this::onEventClick));

        getEventsLiveData().observe(getViewLifecycleOwner(), events -> {
            final var adapter = new EventRecyclerViewAdapter(events, this::onEventClick);
            recyclerView.swapAdapter(adapter, false);
        });

        requestRefresh();

        return recyclerView;
    }
}
