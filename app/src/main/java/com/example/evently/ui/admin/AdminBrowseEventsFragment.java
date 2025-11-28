package com.example.evently.ui.admin;

import java.util.List;
import java.util.function.Consumer;

import android.util.Log;
import android.widget.Toast;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

/**
 * A fragment representing a list of events the admin can browse and interact with.
 */
public class AdminBrowseEventsFragment extends EventsFragment {

    /**
     * Handles clicks on an event row in the Admin Browse list.
     * <p>
     * Uses the Navigation Component to navigate to the Admin view of the Event Details screen,
     * passing the clicked event’s ID as a String argument.
     * @param event The structural representation of the Event view that was clicked.
     */
    @Override
    protected void onEventClick(Event event) {
        var action = HomeFragmentDirections.actionNavHomeToEventDetails(event.eventID());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    /**
     * Supplies the Browse list with all events open and closed.
     * @param callback Callback that will be passed the events into.
     */
    @Override
    protected void initEvents(Consumer<List<Event>> callback) {

        new EventsDB().fetchAllEvents().thenRun(callback).catchE(e -> {
            Log.e("Admin Events", e.toString());
            Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                    .show();
        });
    }
}
