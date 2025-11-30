package com.example.evently.ui.entrant;

import java.util.List;
import java.util.function.Consumer;

import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * A fragment representing a list of events that an entrant has joined
 *
 */
public class JoinedEventsFragment extends EventsFragment {

    /**
     * Handles clicks on a joined event item.
     *
     * @param event the clicked {@link Event}.
     */
    @Override
    protected void onEventClick(Event event) {
        // The action for clicking on the event, pass the event ID to the next event details
        // fragment
        var action = HomeFragmentDirections.actionNavHomeToEventDetails(event.eventID());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    /**
     * Supplies the “Joined” list with placeholder events.
     *
     * @param callback Callback that will be passed the events into.
     */
    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        new EventsDB()
                .fetchEventsByEnrolled(FirebaseAuthUtils.getCurrentEmail())
                .thenRun(callback)
                .catchE(e -> {
                    Log.e("JoinedEvents", e.toString());
                    Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                            .show();
                });
    }
}
