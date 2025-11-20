package com.example.evently.ui.entrant;

import java.util.List;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.model.Event;
import com.example.evently.ui.common.LiveEventsFragment;
import com.example.evently.ui.model.EntrantEventsViewModel;

/**
 * A fragment representing a list of events that an entrant has joined
 *
 */
public class JoinedEventsFragment extends LiveEventsFragment {
    private EntrantEventsViewModel eventsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsViewModel =
                new ViewModelProvider(requireActivity()).get(EntrantEventsViewModel.class);
    }

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
     * Gets the {@link LiveData} of joined events.
     *
     * @return the {@link LiveData} of joined events.
     */
    @Override
    protected LiveData<List<Event>> getEventsLiveData() {
        return eventsViewModel.getFilteredJoinedEvents();
    }

    /**
     * Refreshes the list of joined events.
     */
    @Override
    protected void requestRefresh() {
        eventsViewModel.refreshJoinedEvents().catchE(e -> Toast.makeText(
                        requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                .show());
    }
}
