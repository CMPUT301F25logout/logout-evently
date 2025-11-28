package com.example.evently.ui.entrant;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import android.util.Log;
import android.widget.Toast;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

/**
 * A fragment representing list of events filtered by the search bar
 */
public class SearchEventsListFragment extends EventsFragment {
    private List<Event> eventList = new ArrayList<>();
    public List<Event> filteredList = new ArrayList<>();

    /**
     * Filters events and updates the list view accordingly
     * based on a search term.
     * @param text The search term.
     */
    public void filter(String text) {
        if (adapter == null || text.isEmpty()) {
            adapter.updateEvents(eventList);
            return;
        }

        text = text.toLowerCase();
        filteredList.clear();
        for (Event event : eventList) {
            String eventName = event.toHashMap().get("name").toString();
            if (eventName.toLowerCase().contains(text)) {
                filteredList.add(event);
            }
        }
        adapter.updateEvents(filteredList);
    }

    /**
     * Handles clicks on an event row in the Search list.
     * <p>
     * Uses the Navigation Component to navigate to the Event Details screen,
     * passing the clicked event’s ID as a String argument.
     * @param event The structural representation of the Event view that was clicked.
     */
    @Override
    protected void onEventClick(Event event) {
        // The action for clicking on the event, pass the event ID to the next event details
        // fragment
        var action = SearchEventsFragmentDirections.actionNavSearchToEventDetails(event.eventID());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    /**
     * Supplies the Search list with placeholder events
     * @param callback Callback that will be passed the events into.
     */
    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        new EventsDB()
                .fetchEventsByDate(Timestamp.now(), true)
                .thenRun(events -> {
                    eventList = events;
                    callback.accept(events);
                })
                .catchE(e -> {
                    Log.e("SearchEvents", e.toString());
                    Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                            .show();
                });
    }
}
