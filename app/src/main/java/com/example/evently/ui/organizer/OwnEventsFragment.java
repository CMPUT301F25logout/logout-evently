package com.example.evently.ui.organizer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Fragment that displays the organizer's own events
 */
public class OwnEventsFragment extends EventsFragment {

    // Local, mutable list backing the adapter
    private final ArrayList<Event> events = new ArrayList<>();

    /**
     * Handles clicks on an event row in the organizer list
     *
     * @param event The structural representation of the Event view that was clicked.
     */
    @Override
    protected void onEventClick(Event event) {}

    /**
     * Supplies the layout resource used by this fragment
     *
     * @return {@code R.layout.fragment_organizer_event_list}.
     */
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_organizer_event_list;
    }

    /**
     * Called after the view is created; wires the "Create Event" button and
     * observes for newly created events returned from {@link CreateEventFragment}.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button create = view.findViewById(R.id.btnCreateEvent);
        /*
        if (create != null) {
            create.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigate(R.id.navigate_create_events));
        }
         */

        // Receive result from CreateEventFragment
        var nav = NavHostFragment.findNavController(this);
        nav.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .<Event>getLiveData("new_event")
                .observe(getViewLifecycleOwner(), event -> {
                    if (event != null) {
                        new EventsDB().storeEvent(event).thenRun(v -> {
                            events.add(event);
                            if (adapter != null) {
                                adapter.notifyItemInserted(events.size() - 1);
                            }
                        });
                    }
                });
    }

    /**
     * Provides the initial dataset for the organizer's event list
     * this adds a single placeholder event the first time the list is shown.
     *
     * @param callback Callback that will be passed the events into.
     */
    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        // TODO (chase): Get list of own events by organizer.
        if (events.isEmpty()) {
            new EventsDB()
                    .fetchEventsByOrganizers(FirebaseAuthUtils.getCurrentEmail())
                    .thenRun(callback)
                    .catchE(e -> {
                        Log.e("OwnEvents", e.toString());
                        Toast.makeText(
                                        requireContext(),
                                        "Something went wrong...",
                                        Toast.LENGTH_SHORT)
                                .show();
                    });
        }
        callback.accept(events);
    }
}
