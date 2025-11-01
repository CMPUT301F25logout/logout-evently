package com.example.evently.ui.common;

import java.util.List;
import java.util.function.Consumer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
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
public abstract class EventsFragment extends Fragment {

    /**
     * Adapter to manage the events list dynamically.
     */
    protected EventRecyclerViewAdapter adapter;

    /**
     * This method will be called by onCreateView to set up the events view.
     * It is guaranteed that the activity context will be available at the time of calling.
     * @param callback Callback that will be passed the events into.
     */
    protected abstract void initEvents(Consumer<List<Event>> callback);

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        if (view instanceof RecyclerView recyclerView) {
            // Set the adapter
            Context context = recyclerView.getContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            // Set up the recycler view adapter with the initial list of events (asynchronous).
            initEvents(events -> {
                adapter = new EventRecyclerViewAdapter(events);
                recyclerView.setAdapter(adapter);
            });

            return view;
        } else {
            throw new AssertionError("EventsFragment.onCreateView called with non RecyclerView");
        }
    }
}
