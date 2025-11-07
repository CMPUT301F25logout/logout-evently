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
     * Listener to attach to the event on click.
     * This may be different for the entrant vs organizer event click.
     * @param event The structural representation of the Event view that was clicked.
     */
    protected abstract void onEventClick(Event event);

    /**
     * Supplies the layout resource to inflate for this events list fragment.
     * @return a valid layout resource id (e.g., {@code R.layout.fragment_event_list})
     */
    protected abstract int getLayoutRes();

    /**
     * This method will be called by onCreateView to set up the events view.
     * It is guaranteed that the activity context will be available at the time of calling.
     * @param callback Callback that will be passed the events into.
     */
    protected abstract void initEvents(Consumer<List<Event>> callback);

    /**
     * Inflates the subclass-provided layout, locates the {@link RecyclerView}, connects
     * everything to it
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the inflated root {@link View} of this fragment.
     * @throws AssertionError if the inflated layout does not provide a RecyclerView
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);

        RecyclerView recyclerView = (view instanceof RecyclerView)
                ? (RecyclerView) view
                : view.findViewById(R.id.event_list);

        if (recyclerView == null) {
            throw new AssertionError("EventsFragment.onCreateView called with non RecyclerView");
        }

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Set up the recycler view adapter with the initial list of events (asynchronous).
        initEvents(events -> {
            adapter = new EventRecyclerViewAdapter(events, this::onEventClick);
            recyclerView.setAdapter(adapter);
        });

        return view;
    }
}
