package com.example.evently.ui.organizer;

import java.util.List;
import java.util.function.Consumer;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.SearchedEventsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Organizer version of SearchedEventsFragment. Shows self events filtered by search string.
 */
public class OrganizerSearchedEventsFragment extends SearchedEventsFragment {

    @Override
    protected void updateEventsBy(String target, Consumer<List<Event>> act) {
        final var self = FirebaseAuthUtils.getCurrentEmail();
        new EventsDB().fetchOrganizerEventsBySearchString(self, target).thenRun(act);
    }

    @Override
    protected void onEventClick(Event event) {
        // The action for clicking on the event, pass the event ID to the next event details
        // fragment
        var action = OrganizerSearchEventsFragmentDirections.actionNavSearchToEventDetails(
                event.eventID());
        NavController navController = NavHostFragment.findNavController(requireParentFragment());
        navController.navigate(action);
    }
}
