package com.example.evently.ui.organizer;

import java.util.List;
import java.util.function.Consumer;

import android.util.Log;
import android.widget.Toast;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Fragment that displays the organizer's own events
 */
public class OwnEventsFragment extends EventsFragment {

    /**
     * Handles clicks on an event row in the organizer list
     *
     * @param event The structural representation of the Event view that was clicked.
     */
    @Override
    protected void onEventClick(Event event) {
        var action = HomeFragmentDirections.actionNavHomeToEventDetails(event.eventID());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    /**
     * Provides the initial dataset for the organizer's event list
     * this adds a single placeholder event the first time the list is shown.
     *
     * @param callback Callback that will be passed the events into.
     */
    @Override
    protected void initEvents(Consumer<List<Event>> callback) {

        new EventsDB()
                .fetchEventsByOrganizers(FirebaseAuthUtils.getCurrentEmail())
                .thenRun(callback)
                .catchE(e -> {
                    Log.e("OwnEvents", "Error showing events", e);
                    Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                            .show();
                    callback.accept(java.util.List.of());
                });
    }
}
