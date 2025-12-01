package com.example.evently.ui.entrant;

import java.util.List;
import java.util.function.Consumer;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.SearchedEventsFragment;

/**
 * Entrant version of SearchedEventsFragment. Shows all events filtered by search string.
 */
public class EntrantSearchedEventsFragment extends SearchedEventsFragment {

    @Override
    protected void updateEventsBy(String target, Consumer<List<Event>> act) {
        new EventsDB().fetchEventsBySearchString(target).thenRun(act);
    }

    @Override
    protected void onEventClick(Event event) {
        // The action for clicking on the event, pass the event ID to the next event details
        // fragment
        var action = EntrantSearchEventsFragmentDirections.actionNavSearchToEventDetails(
                event.eventID());
        NavController navController = NavHostFragment.findNavController(requireParentFragment());
        navController.navigate(action);
    }
}
